package cn.nuist.aiarticlewriter.model.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * Image asset media type enum.
 */
@Getter
public enum ImageMediaTypeEnum {

    IMAGE_URL("Remote image URL", "IMAGE_URL", "image/jpeg"),
    BASE64_IMAGE("Base64 encoded image", "BASE64_IMAGE", "image/png"),
    SVG("SVG image content", "SVG", "image/svg+xml"),
    MERMAID("Mermaid source content", "MERMAID", "text/plain");

    private final String text;

    private final String value;

    private final String contentType;

    ImageMediaTypeEnum(String text, String value, String contentType) {
        this.text = text;
        this.value = value;
        this.contentType = contentType;
    }

    /**
     * Get enum by value.
     *
     * @param value media type value
     * @return image media type enum
     */
    public static ImageMediaTypeEnum getEnumByValue(String value) {
        return Arrays.stream(values())
                .filter(mediaTypeEnum -> mediaTypeEnum.value.equals(value))
                .findFirst()
                .orElse(null);
    }
}
