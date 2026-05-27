package cn.nuist.aiarticlewriter.model.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * Payment product type enum.
 */
@Getter
public enum PaymentProductTypeEnum {

    VIP_PERMANENT("Permanent VIP membership", "VIP_PERMANENT");

    private final String text;

    private final String value;

    PaymentProductTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static PaymentProductTypeEnum getEnumByValue(String value) {
        return Arrays.stream(values())
                .filter(productTypeEnum -> productTypeEnum.value.equals(value))
                .findFirst()
                .orElse(null);
    }
}
