package cn.nuist.aiarticlewriter.model.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * Article generation workflow step enum.
 */
@Getter
public enum ArticleStepEnum {

    INIT("Initial state", "INIT"),
    TITLE("Title generation", "TITLE"),
    OUTLINE("Outline generation", "OUTLINE"),
    CONTENT("Content generation", "CONTENT"),
    IMAGE_REQUIREMENT("Image requirement generation", "IMAGE_REQUIREMENT"),
    IMAGE_GENERATION("Image generation", "IMAGE_GENERATION"),
    ASSEMBLE("Content assembly", "ASSEMBLE"),
    COMPLETED("Completed", "COMPLETED"),
    FAILED("Failed", "FAILED");

    private final String text;

    private final String value;

    ArticleStepEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * Get enum by value.
     *
     * @param value step value
     * @return article step enum
     */
    public static ArticleStepEnum getEnumByValue(String value) {
        return Arrays.stream(values())
                .filter(articleStepEnum -> articleStepEnum.value.equals(value))
                .findFirst()
                .orElse(null);
    }
}
