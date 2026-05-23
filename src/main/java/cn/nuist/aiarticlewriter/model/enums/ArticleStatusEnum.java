package cn.nuist.aiarticlewriter.model.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * Article generation status enum.
 */
@Getter
public enum ArticleStatusEnum {

    PENDING("Pending", "PENDING"),
    PROCESSING("Processing", "PROCESSING"),
    COMPLETED("Completed", "COMPLETED"),
    FAILED("Failed", "FAILED");

    private final String text;

    private final String value;

    ArticleStatusEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * Get enum by value.
     *
     * @param value status value
     * @return article status enum
     */
    public static ArticleStatusEnum getEnumByValue(String value) {
        return Arrays.stream(values())
                .filter(articleStatusEnum -> articleStatusEnum.value.equals(value))
                .findFirst()
                .orElse(null);
    }
}
