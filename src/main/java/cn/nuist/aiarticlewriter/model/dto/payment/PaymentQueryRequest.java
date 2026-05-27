package cn.nuist.aiarticlewriter.model.dto.payment;

import cn.nuist.aiarticlewriter.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * Payment record query request.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private String stripeSessionId;

    private String status;

    private String productType;
}
