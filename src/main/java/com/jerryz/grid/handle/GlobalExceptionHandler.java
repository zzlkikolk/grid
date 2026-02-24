package com.jerryz.grid.handle;

import com.jerryz.grid.pojo.ro.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * @author zhangzhilin
 * @version 2026
 * @date 2026/02/06 16:48
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNotFoundException(NoHandlerFoundException e) {
        log.error("请求路径不存在: {}", e.getRequestURL(), e);
        return Result.error(HttpStatus.NOT_FOUND.value(), "请求路径不存在: " + e.getRequestURL());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleValidException(MethodArgumentNotValidException e) {

        String msg = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse("参数校验失败");

        return Result.error(HttpStatus.BAD_REQUEST.value(),msg);
    }

    /**
     * 处理业务异常
     * 您可以在代码中抛出ServiceException来处理业务逻辑错误
     */
    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleServiceException(ServiceException e) {
        log.error("业务异常: {}", e.getMessage(), e);
        return Result.error(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleNullPointerException(NullPointerException e) {
        log.error("空指针异常", e);
        return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), "系统内部错误: 空指针异常");
    }

    /**
     * 处理所有未捕获的其他异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常, 请求路径: {}, 异常信息: {}",
                request.getRequestURI(), e.getMessage(), e);
        return Result.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "系统内部错误: " + (e.getMessage() != null ? e.getMessage() : "未知错误"));
    }

    /**
     * 自定义业务异常类
     */
    public static class ServiceException extends RuntimeException {
        public ServiceException(String message) {
            super(message);
        }

        public ServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
