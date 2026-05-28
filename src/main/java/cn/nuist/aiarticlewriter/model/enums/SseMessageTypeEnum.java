package cn.nuist.aiarticlewriter.model.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * SSE message type enum for article generation.
 */
@Getter
public enum SseMessageTypeEnum {

    AGENT1_COMPLETE("AGENT1_COMPLETE", "Title generation completed"),
    WAITING_USER_INPUT("WAITING_USER_INPUT", "Waiting for user input"),
    AGENT2_STREAMING("AGENT2_STREAMING", "Outline streaming output"),
    AGENT2_COMPLETE("AGENT2_COMPLETE", "Outline generation completed"),
    AGENT3_STREAMING("AGENT3_STREAMING", "Content streaming output"),
    AGENT3_COMPLETE("AGENT3_COMPLETE", "Content generation completed"),
    AGENT4_COMPLETE("AGENT4_COMPLETE", "Image requirement analysis completed"),
    IMAGE_COMPLETE("IMAGE_COMPLETE", "Single image completed"),
    AGENT5_COMPLETE("AGENT5_COMPLETE", "Image generation completed"),
    MERGE_COMPLETE("MERGE_COMPLETE", "Article image merge completed"),
    ALL_COMPLETE("ALL_COMPLETE", "All article generation steps completed"),
    ERROR("ERROR", "Error");

    /**
     * Message type value.
     */
    private final String value;

    /**
     * Message type description.
     */
    private final String description;

    SseMessageTypeEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * Get streaming output message prefix.
     *
     * @return message prefix with colon
     */
    public String getStreamingPrefix() {
        return this.value + ":";
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
