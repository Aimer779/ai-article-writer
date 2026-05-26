package cn.nuist.aiarticlewriter.model.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * Image acquisition method enum.
 */
@Getter
public enum ImageMethodEnum {

    PEXELS("Pexels image search", "PEXELS"),
    AI_GENERATION("AI image generation", "AI_GENERATION"),
    MERMAID("Mermaid diagram generation", "MERMAID"),
    MEME("Meme image generation", "MEME"),
    SVG_DIAGRAM("SVG diagram generation", "SVG_DIAGRAM"),
    PICSUM("Picsum fallback image", "PICSUM"),
    UNKNOWN("Unknown image method", "UNKNOWN");

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
