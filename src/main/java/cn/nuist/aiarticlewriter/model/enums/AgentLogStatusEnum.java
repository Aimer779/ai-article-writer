package cn.nuist.aiarticlewriter.model.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * Agent execution log status enum.
 */
@Getter
public enum AgentLogStatusEnum {

    RUNNING("Running", "RUNNING"),
    SUCCESS("Success", "SUCCESS"),
    FAILED("Failed", "FAILED");

    private final String text;

    private final String value;

    AgentLogStatusEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    public static AgentLogStatusEnum getEnumByValue(String value) {
        return Arrays.stream(values())
                .filter(statusEnum -> statusEnum.value.equals(value))
                .findFirst()
                .orElse(null);
    }
}
