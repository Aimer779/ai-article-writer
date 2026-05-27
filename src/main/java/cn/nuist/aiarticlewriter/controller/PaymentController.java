package cn.nuist.aiarticlewriter.controller;

import cn.nuist.aiarticlewriter.common.BaseResponse;
import cn.nuist.aiarticlewriter.common.ResultUtils;
import cn.nuist.aiarticlewriter.exception.ErrorCode;
import cn.nuist.aiarticlewriter.exception.ThrowUtils;
import cn.nuist.aiarticlewriter.model.dto.payment.PaymentCreateRequest;
import cn.nuist.aiarticlewriter.model.dto.payment.PaymentQueryRequest;
import cn.nuist.aiarticlewriter.model.entity.User;
import cn.nuist.aiarticlewriter.model.vo.PaymentRecordVO;
import cn.nuist.aiarticlewriter.model.vo.PaymentSessionVO;
import cn.nuist.aiarticlewriter.service.PaymentService;
import cn.nuist.aiarticlewriter.service.UserService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Payment controller.
 */
@RestController
@RequestMapping("/payment")
@Tag(name = "Payment Controller")
public class PaymentController {

    @Resource
    private PaymentService paymentService;

    @Resource
    private UserService userService;

    /**
     * Create Stripe Checkout session.
     *
     * @param paymentCreateRequest payment create request
     * @param request              HTTP request
     * @return Checkout session
     */
    @PostMapping("/checkout/session")
    @Operation(summary = "Create Stripe Checkout session")
    public BaseResponse<PaymentSessionVO> createCheckoutSession(
            @RequestBody PaymentCreateRequest paymentCreateRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(paymentCreateRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        PaymentSessionVO paymentSessionVO = paymentService.createCheckoutSession(
                paymentCreateRequest.getProductType(), loginUser);
        return ResultUtils.success(paymentSessionVO);
    }

    /**
     * Stripe webhook callback.
     *
     * @param payload         raw payload
     * @param stripeSignature Stripe signature header
     * @return handled result
     */
    @PostMapping("/stripe/webhook")
    @Operation(summary = "Stripe webhook callback")
    public BaseResponse<Boolean> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String stripeSignature) {
        boolean result = paymentService.handleStripeWebhook(payload, stripeSignature);
        return ResultUtils.success(result);
    }

    /**
     * Get payment record by id.
     *
     * @param id      payment record id
     * @param request HTTP request
     * @return payment record view object
     */
    @GetMapping("/get")
    @Operation(summary = "Get payment record by id")
    public BaseResponse<PaymentRecordVO> getPaymentRecordById(@RequestParam Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        PaymentRecordVO paymentRecordVO = paymentService.getPaymentRecordVOById(id, loginUser);
        return ResultUtils.success(paymentRecordVO);
    }

    /**
     * Page payment records.
     *
     * @param paymentQueryRequest query request
     * @param request             HTTP request
     * @return payment record page
     */
    @PostMapping("/list/page")
    @Operation(summary = "Page payment records")
    public BaseResponse<Page<PaymentRecordVO>> listPaymentRecordByPage(
            @RequestBody PaymentQueryRequest paymentQueryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(paymentQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Page<PaymentRecordVO> paymentRecordPage = paymentService.listPaymentRecordByPage(paymentQueryRequest,
                loginUser);
        return ResultUtils.success(paymentRecordPage);
    }
}
