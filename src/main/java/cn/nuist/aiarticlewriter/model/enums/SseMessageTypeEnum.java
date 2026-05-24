package cn.nuist.aiarticlewriter.model.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * SSE message type enum for streaming article generation.
 */
@Getter
public enum SseMessageTypeEnum {

    TITLE("Title generation message", "TITLE", "[TITLE]"),
    OUTLINE("Outline generation message", "OUTLINE", "[OUTLINE]"),
    CONTENT("Content generation message", "CONTENT", "[CONTENT]"),
    IMAGE("Image generation message", "IMAGE", "[IMAGE]"),
    DONE("Workflow done message", "DONE", "[DONE]"),
    ERROR("Workflow error message", "ERROR", "[ERROR]");

    private final String text;

    private final String value;

    private final String streamingPrefix;

    SseMessageTypeEnum(String text, String value, String streamingPrefix) {
        this.text = text;
        this.value = value;
        this.streamingPrefix = streamingPrefix;
    }

    /**
     * Get enum by value.
     *
     * @param value message type value
     * @return SSE message type enum
     */
    public static SseMessageTypeEnum getEnumByValue(String value) {
        return Arrays.stream(values())
                .filter(sseMessageTypeEnum -> sseMessageTypeEnum.value.equals(value))
                .findFirst()
                .orElse(null);
    }
}
