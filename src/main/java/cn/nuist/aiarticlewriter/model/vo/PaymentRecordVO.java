package cn.nuist.aiarticlewriter.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment record view object.
 */
@Data
public class PaymentRecordVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private Long userId;

    private String stripeSessionId;

    private String stripePaymentIntentId;

    private BigDecimal amount;

    private String currency;

    private String status;

    private String productType;

    private String description;

    private LocalDateTime refundTime;

    private String refundReason;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
