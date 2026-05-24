package cn.nuist.aiarticlewriter.model.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * Image acquisition method enum.
 */
@Getter
public enum ImageMethodEnum {

    PEXELS("Pexels image search", "PEXELS"),
    PICSUM("Picsum fallback image", "PICSUM"),
    AI_GENERATION("AI image generation", "AI_GENERATION");

    private final String text;

    private final String value;

    ImageMethodEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * Get enum by value.
     *
     * @param value method value
     * @return image method enum
     */
    public static ImageMethodEnum getEnumByValue(String value) {
        return Arrays.stream(values())
                .filter(imageMethodEnum -> imageMethodEnum.value.equals(value))
                .findFirst()
                .orElse(null);
    }
}
