package cn.nuist.aiarticlewriter.service;

import cn.nuist.aiarticlewriter.model.dto.payment.PaymentQueryRequest;
import cn.nuist.aiarticlewriter.model.entity.PaymentRecord;
import cn.nuist.aiarticlewriter.model.entity.User;
import cn.nuist.aiarticlewriter.model.vo.PaymentRecordVO;
import cn.nuist.aiarticlewriter.model.vo.PaymentSessionVO;
import com.mybatisflex.core.paginate.Page;

/**
 * Payment service.
 */
public interface PaymentService {

    /**
     * Create a Stripe Checkout session.
     *
     * @param productType product type
     * @param loginUser   current user
     * @return Checkout session view object
     */
    PaymentSessionVO createCheckoutSession(String productType, User loginUser);

    /**
     * Handle Stripe webhook event.
     *
     * @param payload         raw webhook body
     * @param stripeSignature Stripe-Signature header
     * @return handled result
     */
    boolean handleStripeWebhook(String payload, String stripeSignature);

    /**
     * Get payment record view object by id.
     *
     * @param id        payment record id
     * @param loginUser current user
     * @return payment record view object
     */
    PaymentRecordVO getPaymentRecordVOById(long id, User loginUser);

    /**
     * Page payment record view objects.
     *
     * @param request   query request
     * @param loginUser current user
     * @return payment record page
     */
    Page<PaymentRecordVO> listPaymentRecordByPage(PaymentQueryRequest request, User loginUser);

    /**
     * Convert payment record to view object.
     *
     * @param paymentRecord payment record entity
     * @return payment record view object
     */
    PaymentRecordVO getPaymentRecordVO(PaymentRecord paymentRecord);
}
