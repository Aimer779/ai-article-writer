package cn.nuist.aiarticlewriter.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Stripe Checkout session view object.
 */
@Data
public class PaymentSessionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long paymentRecordId;

    private String stripeSessionId;

    private String checkoutUrl;
}
