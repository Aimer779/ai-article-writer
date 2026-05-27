package cn.nuist.aiarticlewriter.model.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * Payment status enum.
 */
@Getter
public enum PaymentStatusEnum {

    PENDING("Pending", "PENDING"),
    SUCCEEDED("Succeeded", "SUCCEEDED"),
    FAILED("Failed", "FAILED"),
    REFUNDED("Refunded", "REFUNDED");

    private final String text;

    private final String value;

    PaymentStatusEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static PaymentStatusEnum getEnumByValue(String value) {
        return Arrays.stream(values())
                .filter(statusEnum -> statusEnum.value.equals(value))
                .findFirst()
                .orElse(null);
    }
}
