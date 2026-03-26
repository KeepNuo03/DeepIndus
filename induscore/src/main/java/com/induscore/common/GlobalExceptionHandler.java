package com.induscore.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理，保证响应结构统一。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常。
     */
    @ExceptionHandler(ApiException.class)
    public ApiResponse<Object> handleApiException(ApiException ex) {
        log.warn("api_exception code={} message={}", ex.getCode(), ex.getMessage());
        return ApiResponse.error(ex.getCode(), ex.getMessage());
    }

    /**
     * 处理参数校验异常。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Object> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().isEmpty()
                ? "请求参数错误"
                : ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("validation_exception message={}", message);
        return ApiResponse.error(400, message);
    }

    /**
     * 处理类型转换异常（如 page=undefined）。
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ApiResponse<Object> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String field = ex.getName() == null ? "参数" : ex.getName();
        String message = field + " 参数类型错误";
        log.warn("type_mismatch field={} value={} requiredType={}",
                ex.getName(), ex.getValue(), ex.getRequiredType());
        return ApiResponse.error(400, message);
    }

    /**
     * 处理兜底异常，避免栈信息泄露。
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Object> handleException(Exception ex) {
        log.error("unhandled_exception", ex);
        return ApiResponse.error(500, "服务器错误");
    }
}
