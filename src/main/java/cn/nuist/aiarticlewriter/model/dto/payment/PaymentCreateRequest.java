package cn.nuist.aiarticlewriter.model.dto.payment;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Payment checkout creation request.
 */
@Data
public class PaymentCreateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Product type. Defaults to VIP_PERMANENT.
     */
    private String productType;
}
