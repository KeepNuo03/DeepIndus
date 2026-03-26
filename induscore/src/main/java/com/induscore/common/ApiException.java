package com.induscore.common;

/**
 * 业务异常，携带业务状态码。
 */
public class ApiException extends RuntimeException {
    /**
     * 业务状态码
     */
    private final int code;

    public ApiException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
