package cn.nuist.aiarticlewriter.model.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * User role enum.
 */
@Getter
public enum UserRoleEnum {

    USER("User", "user"),
    ADMIN("Admin", "admin");

    private final String text;

    private final String value;

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * Get enum by value.
     *
     * @param value role value
     * @return user role enum
     */
    public static UserRoleEnum getEnumByValue(String value) {
        return Arrays.stream(values())
                .filter(userRoleEnum -> userRoleEnum.value.equals(value))
                .findFirst()
                .orElse(null);
    }
}
