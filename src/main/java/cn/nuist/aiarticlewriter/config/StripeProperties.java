package cn.nuist.aiarticlewriter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Stripe payment configuration properties.
 */
@Data
@Component
@ConfigurationProperties(prefix = "stripe")
public class StripeProperties {

    /**
     * Stripe secret API key.
     */
    private String secretKey;

    /**
     * Stripe webhook signing secret.
     */
    private String webhookSecret;

    /**
     * Checkout success redirect URL.
     */
    private String successUrl;

    /**
     * Checkout cancel redirect URL.
     */
    private String cancelUrl;

    /**
     * Payment currency.
     */
    private String currency = "usd";

    /**
     * Permanent VIP price in major currency unit.
     */
    private BigDecimal vipPermanentPrice = new BigDecimal("9.99");

    /**
     * Stripe product display name.
     */
    private String vipProductName = "AI Article Writer VIP";
}
