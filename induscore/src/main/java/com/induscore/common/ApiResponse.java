package com.induscore.common;

/**
 * 统一 API 响应结构。
 *
 * @param <T> 业务数据类型
 */
public class ApiResponse<T> {
    /**
     * 业务状态码（200 成功，其它为错误）
     */
    private final int code;
    /**
     * 提示信息
     */
    private final String message;
    /**
     * 业务数据
     */
    private final T data;
    /**
     * 服务器时间戳（毫秒）
     */
    private final long timestamp;

    private ApiResponse(int code, String message, T data, long timestamp) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = timestamp;
    }

    /**
     * 生成成功响应（带消息和数据）
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data, System.currentTimeMillis());
    }

    /**
     * 生成成功响应（默认消息）
     */
    public static <T> ApiResponse<T> success(T data) {
        return success("success", data);
    }

    /**
     * 生成错误响应（仅返回 code 和 message）
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null, System.currentTimeMillis());
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
