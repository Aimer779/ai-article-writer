package cn.nuist.aiarticlewriter.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "Invalid request parameters"),
    NOT_LOGIN_ERROR(40100, "Not logged in"),
    NO_AUTH_ERROR(40101, "Unauthorized"),
    TOO_MANY_REQUEST(42900, "Too many requests"),
    NOT_FOUND_ERROR(40400, "Resource not found"),
    FORBIDDEN_ERROR(40300, "Access denied"),
    SYSTEM_ERROR(50000, "Internal system error"),
    OPERATION_ERROR(50001, "Operation failed");

    /**
     * Status code
     */
    private final int code;

    /**
     * Message
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
