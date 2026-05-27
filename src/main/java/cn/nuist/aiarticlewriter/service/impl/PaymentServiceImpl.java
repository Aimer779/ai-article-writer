package cn.nuist.aiarticlewriter.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.nuist.aiarticlewriter.config.StripeProperties;
import cn.nuist.aiarticlewriter.constant.UserConstant;
import cn.nuist.aiarticlewriter.exception.BusinessException;
import cn.nuist.aiarticlewriter.exception.ErrorCode;
import cn.nuist.aiarticlewriter.exception.ThrowUtils;
import cn.nuist.aiarticlewriter.mapper.PaymentRecordMapper;
import cn.nuist.aiarticlewriter.mapper.UserMapper;
import cn.nuist.aiarticlewriter.model.dto.payment.PaymentQueryRequest;
import cn.nuist.aiarticlewriter.model.entity.PaymentRecord;
import cn.nuist.aiarticlewriter.model.entity.User;
import cn.nuist.aiarticlewriter.model.enums.PaymentProductTypeEnum;
import cn.nuist.aiarticlewriter.model.enums.PaymentStatusEnum;
import cn.nuist.aiarticlewriter.model.vo.PaymentRecordVO;
import cn.nuist.aiarticlewriter.model.vo.PaymentSessionVO;
import cn.nuist.aiarticlewriter.service.PaymentService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Payment service implementation.
 */
@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private static final long MAX_PAGE_SIZE = 50;

    private static final Set<String> PAYMENT_SORT_FIELD_SET = Set.of(
            "id",
            "amount",
            "status",
            "productType",
            "createTime",
            "updateTime"
    );

    @Resource
    private StripeProperties stripeProperties;

    @Resource
    private PaymentRecordMapper paymentRecordMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentSessionVO createCheckoutSession(String productType, User loginUser) {
        validateLoginUser(loginUser);
        PaymentProductTypeEnum productTypeEnum = PaymentProductTypeEnum.getEnumByValue(
                StrUtil.blankToDefault(productType, PaymentProductTypeEnum.VIP_PERMANENT.getValue()));
        ThrowUtils.throwIf(productTypeEnum == null, ErrorCode.PARAMS_ERROR, "Product type is invalid");
        validateStripeCheckoutConfig();

        BigDecimal amount = resolveAmount(productTypeEnum);
        LocalDateTime now = LocalDateTime.now();
        PaymentRecord paymentRecord = PaymentRecord.builder()
                .userId(loginUser.getId())
                .amount(amount)
                .currency(stripeProperties.getCurrency())
                .status(PaymentStatusEnum.PENDING.getValue())
                .productType(productTypeEnum.getValue())
                .description(productTypeEnum.getText())
                .createTime(now)
                .updateTime(now)
                .build();
        int insertResult = paymentRecordMapper.insert(paymentRecord);
        ThrowUtils.throwIf(insertResult <= 0 || paymentRecord.getId() == null, ErrorCode.OPERATION_ERROR,
                "Create payment record failed");

        try {
            Stripe.apiKey = stripeProperties.getSecretKey();
            SessionCreateParams params = buildCheckoutSessionParams(paymentRecord, productTypeEnum);
            Session session = Session.create(params);
            paymentRecord.setStripeSessionId(session.getId());
            paymentRecord.setUpdateTime(LocalDateTime.now());
            int updateResult = paymentRecordMapper.update(paymentRecord);
            ThrowUtils.throwIf(updateResult <= 0, ErrorCode.OPERATION_ERROR, "Update payment record failed");

            PaymentSessionVO paymentSessionVO = new PaymentSessionVO();
            paymentSessionVO.setPaymentRecordId(paymentRecord.getId());
            paymentSessionVO.setStripeSessionId(session.getId());
            paymentSessionVO.setCheckoutUrl(session.getUrl());
            return paymentSessionVO;
        } catch (StripeException e) {
            log.error("Create Stripe checkout session failed, paymentRecordId={}", paymentRecord.getId(), e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Create Stripe checkout session failed");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleStripeWebhook(String payload, String stripeSignature) {
        ThrowUtils.throwIf(StrUtil.hasBlank(payload, stripeSignature), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(stripeProperties.getWebhookSecret()), ErrorCode.OPERATION_ERROR,
                "Stripe webhook secret is not configured");

        Event event;
        try {
            event = Webhook.constructEvent(payload, stripeSignature, stripeProperties.getWebhookSecret());
        } catch (SignatureVerificationException e) {
            log.warn("Stripe webhook signature verification failed");
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "Invalid Stripe webhook signature");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR,
                            "Stripe checkout session payload is invalid"));
            markPaymentSucceeded(session);
        } else if ("checkout.session.async_payment_failed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer()
                    .getObject()
                    .orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR,
                            "Stripe checkout session payload is invalid"));
            markPaymentFailed(session);
        } else {
            log.info("Stripe webhook ignored, type={}", event.getType());
        }
        return true;
    }

    @Override
    public PaymentRecordVO getPaymentRecordVOById(long id, User loginUser) {
        validateLoginUser(loginUser);
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR, "Payment record id is invalid");
        PaymentRecord paymentRecord = paymentRecordMapper.selectOneById(id);
        ThrowUtils.throwIf(paymentRecord == null, ErrorCode.NOT_FOUND_ERROR, "Payment record does not exist");
        validatePaymentRecordAccess(paymentRecord, loginUser);
        return getPaymentRecordVO(paymentRecord);
    }

    @Override
    public Page<PaymentRecordVO> listPaymentRecordByPage(PaymentQueryRequest request, User loginUser) {
        validateLoginUser(loginUser);
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getPageNum() <= 0, ErrorCode.PARAMS_ERROR, "Page number is invalid");
        ThrowUtils.throwIf(request.getPageSize() <= 0 || request.getPageSize() > MAX_PAGE_SIZE,
                ErrorCode.PARAMS_ERROR, "Page size is invalid");

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("id", request.getId(), request.getId() != null)
                .eq("stripeSessionId", request.getStripeSessionId(), StrUtil.isNotBlank(request.getStripeSessionId()))
                .eq("status", request.getStatus(), StrUtil.isNotBlank(request.getStatus()))
                .eq("productType", request.getProductType(), StrUtil.isNotBlank(request.getProductType()));
        if (isAdmin(loginUser)) {
            queryWrapper.eq("userId", request.getUserId(), request.getUserId() != null);
        } else {
            queryWrapper.eq("userId", loginUser.getId());
        }

        String finalSortField = StrUtil.blankToDefault(request.getSortField(), "createTime");
        ThrowUtils.throwIf(!PAYMENT_SORT_FIELD_SET.contains(finalSortField), ErrorCode.PARAMS_ERROR,
                "Sort field is invalid");
        boolean isAsc = "ascend".equals(request.getSortOrder());
        queryWrapper.orderBy(finalSortField, isAsc);

        Page<PaymentRecord> paymentRecordPage = paymentRecordMapper.paginate(request.getPageNum(),
                request.getPageSize(), queryWrapper);
        return paymentRecordPage.map(this::getPaymentRecordVO);
    }

    @Override
    public PaymentRecordVO getPaymentRecordVO(PaymentRecord paymentRecord) {
        if (paymentRecord == null) {
            return null;
        }
        PaymentRecordVO paymentRecordVO = new PaymentRecordVO();
        BeanUtils.copyProperties(paymentRecord, paymentRecordVO);
        return paymentRecordVO;
    }

    private SessionCreateParams buildCheckoutSessionParams(PaymentRecord paymentRecord,
            PaymentProductTypeEnum productTypeEnum) {
        long unitAmount = paymentRecord.getAmount()
                .movePointRight(2)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
        return SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(stripeProperties.getSuccessUrl())
                .setCancelUrl(stripeProperties.getCancelUrl())
                .putMetadata("paymentRecordId", String.valueOf(paymentRecord.getId()))
                .putMetadata("userId", String.valueOf(paymentRecord.getUserId()))
                .putMetadata("productType", productTypeEnum.getValue())
                .addLineItem(SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(paymentRecord.getCurrency())
                                .setUnitAmount(unitAmount)
                                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName(stripeProperties.getVipProductName())
                                        .build())
                                .build())
                        .build())
                .build();
    }

    private void markPaymentSucceeded(Session session) {
        if (StrUtil.isNotBlank(session.getPaymentStatus()) && !"paid".equals(session.getPaymentStatus())) {
            log.info("Stripe checkout session is completed but not paid, sessionId={}, paymentStatus={}",
                    session.getId(), session.getPaymentStatus());
            return;
        }
        PaymentRecord paymentRecord = getPaymentRecordBySession(session);
        if (PaymentStatusEnum.SUCCEEDED.getValue().equals(paymentRecord.getStatus())) {
            return;
        }
        paymentRecord.setStatus(PaymentStatusEnum.SUCCEEDED.getValue());
        paymentRecord.setStripePaymentIntentId(session.getPaymentIntent());
        paymentRecord.setUpdateTime(LocalDateTime.now());
        int paymentUpdateResult = paymentRecordMapper.update(paymentRecord);
        ThrowUtils.throwIf(paymentUpdateResult <= 0, ErrorCode.OPERATION_ERROR, "Update payment status failed");

        User user = userMapper.selectOneById(paymentRecord.getUserId());
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "Payment user does not exist");
        user.setVipTime(LocalDateTime.now());
        if (UserConstant.DEFAULT_ROLE.equals(user.getUserRole())) {
            user.setUserRole(UserConstant.VIP_ROLE);
        }
        user.setUpdateTime(LocalDateTime.now());
        int userUpdateResult = userMapper.update(user);
        ThrowUtils.throwIf(userUpdateResult <= 0, ErrorCode.OPERATION_ERROR, "Activate VIP failed");
        log.info("Payment succeeded and VIP activated, paymentRecordId={}, userId={}",
                paymentRecord.getId(), paymentRecord.getUserId());
    }

    private void markPaymentFailed(Session session) {
        PaymentRecord paymentRecord = getPaymentRecordBySession(session);
        if (!PaymentStatusEnum.PENDING.getValue().equals(paymentRecord.getStatus())) {
            return;
        }
        paymentRecord.setStatus(PaymentStatusEnum.FAILED.getValue());
        paymentRecord.setStripePaymentIntentId(session.getPaymentIntent());
        paymentRecord.setUpdateTime(LocalDateTime.now());
        int result = paymentRecordMapper.update(paymentRecord);
        ThrowUtils.throwIf(result <= 0, ErrorCode.OPERATION_ERROR, "Update payment status failed");
    }

    private PaymentRecord getPaymentRecordBySession(Session session) {
        ThrowUtils.throwIf(session == null || StrUtil.isBlank(session.getId()), ErrorCode.PARAMS_ERROR,
                "Stripe checkout session is invalid");
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("stripeSessionId", session.getId());
        PaymentRecord paymentRecord = paymentRecordMapper.selectOneByQuery(queryWrapper);
        ThrowUtils.throwIf(paymentRecord == null, ErrorCode.NOT_FOUND_ERROR, "Payment record does not exist");
        return paymentRecord;
    }

    private BigDecimal resolveAmount(PaymentProductTypeEnum productTypeEnum) {
        if (PaymentProductTypeEnum.VIP_PERMANENT.equals(productTypeEnum)) {
            return stripeProperties.getVipPermanentPrice();
        }
        throw new BusinessException(ErrorCode.PARAMS_ERROR, "Product type is invalid");
    }

    private void validateStripeCheckoutConfig() {
        ThrowUtils.throwIf(StrUtil.hasBlank(stripeProperties.getSecretKey(), stripeProperties.getSuccessUrl(),
                stripeProperties.getCancelUrl(), stripeProperties.getCurrency()), ErrorCode.OPERATION_ERROR,
                "Stripe checkout is not configured");
        ThrowUtils.throwIf(stripeProperties.getVipPermanentPrice() == null
                        || stripeProperties.getVipPermanentPrice().compareTo(BigDecimal.ZERO) <= 0,
                ErrorCode.OPERATION_ERROR, "Stripe VIP price is invalid");
    }

    private void validateLoginUser(User loginUser) {
        ThrowUtils.throwIf(loginUser == null || loginUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
    }

    private void validatePaymentRecordAccess(PaymentRecord paymentRecord, User loginUser) {
        if (isAdmin(loginUser)) {
            return;
        }
        ThrowUtils.throwIf(paymentRecord.getUserId() == null || !paymentRecord.getUserId().equals(loginUser.getId()),
                ErrorCode.NO_AUTH_ERROR);
    }

    private boolean isAdmin(User loginUser) {
        return loginUser != null && UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
    }
}
