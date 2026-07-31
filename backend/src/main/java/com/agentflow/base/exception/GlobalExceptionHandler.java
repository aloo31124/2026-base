package com.agentflow.base.exception;

import com.agentflow.base.model.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex) {
        log.warn("業務例外: {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    ResponseEntity<ApiResponse<Void>> handleValidation(Exception ex) {
        String message = ex instanceof MethodArgumentNotValidException validation
            ? validation.getBindingResult().getFieldErrors().stream().map(e -> e.getField() + ": " + e.getDefaultMessage()).collect(Collectors.joining("; "))
            : ex.getMessage();
        log.warn("輸入驗證失敗: {}", message);
        return ResponseEntity.badRequest().body(ApiResponse.error(message));
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiResponse<Void>> handleDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("權限不足: {}", ex.getMessage());
        String message;
        if (request.getRequestURI().startsWith("/api/admin/company-supervisor-management")) {
            message = "[公司主管管理] [api] 無系統管理員權限。";
        } else if (request.getRequestURI().startsWith("/api/task-assignment")) {
            message = "[任務指派] [api] 無主管權限。";
        } else if (request.getRequestURI().startsWith("/api/admin/registration-management")) {
            message = "[註冊登入管理] [api] 無系統管理員權限。";
        } else {
            message = "[使用者角色] [api] 無系統管理員權限。";
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(message));
    }

    /**
     * 將帳密驗證失敗統一轉為 401，避免洩漏帳號是否存在或內部驗證細節。
     */
    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex) {
        log.warn("帳密驗證失敗");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error("帳號或密碼錯誤。"));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> handleUnknown(Exception ex) {
        log.error("未預期系統例外", ex);
        return ResponseEntity.internalServerError().body(ApiResponse.error("系統處理失敗，請聯絡系統管理員。"));
    }
}
