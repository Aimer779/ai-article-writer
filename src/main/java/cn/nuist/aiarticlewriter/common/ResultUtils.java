package cn.nuist.aiarticlewriter.common;

import cn.nuist.aiarticlewriter.exception.ErrorCode;

/**
 * Utility class for constructing response results
 */
public class ResultUtils {

    /**
     * Success
     *
     * @param data data
     * @param <T>  data type
     * @return response
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * Failure
     *
     * @param errorCode error code
     * @return response
     */
    public static BaseResponse<?> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * Failure
     *
     * @param code    error code
     * @param message error message
     * @return response
     */
    public static BaseResponse<?> error(int code, String message) {
        return new BaseResponse<>(code, null, message);
    }

    /**
     * Failure
     *
     * @param errorCode error code
     * @return response
     */
    public static BaseResponse<?> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode.getCode(), null, message);
    }
}
