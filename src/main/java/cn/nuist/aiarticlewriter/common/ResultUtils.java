package cn.nuist.aiarticlewriter.common;

import cn.nuist.aiarticlewriter.exception.ErrorCode;

/**
 * Utility class for constructing response results.
 */
public class ResultUtils {

    /**
     * Success response.
     *
     * @param data response data
     * @param <T>  response data type
     * @return response result
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * Failure response.
     *
     * @param errorCode error code
     * @return response result
     */
    public static BaseResponse<?> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * Failure response.
     *
     * @param code    error code
     * @param message error message
     * @return response result
     */
    public static BaseResponse<?> error(int code, String message) {
        return new BaseResponse<>(code, null, message);
    }

    /**
     * Failure response.
     *
     * @param errorCode error code
     * @param message   error message
     * @return response result
     */
    public static BaseResponse<?> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode.getCode(), null, message);
    }
}
