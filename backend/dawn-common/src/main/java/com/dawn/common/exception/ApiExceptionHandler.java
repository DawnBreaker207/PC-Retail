package com.dawn.common.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import com.dawn.common.exception.payload.ExceptionMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.ZonedDateTime;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {


    private static final HttpStatus DEFAULT_STATUS = HttpStatus.BAD_REQUEST;

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ExceptionMessage> handleApiRequestException(ApiException e) {
        log.info("**ApiExceptionHandler controller, handler API request*\n");
        return buildResponse(e.getStatus(), e.getMessage());
    }

    //
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionMessage> handlerValidationException(final MethodArgumentNotValidException ex) {
        String errorMsg = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Invalid request");
        return buildResponse(DEFAULT_STATUS, errorMsg);
    }

    //
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ExceptionMessage> handlerConstraintViolationException(final ConstraintViolationException ex) {
        String errorMsg = ex.getConstraintViolations()
                .stream()
                .map(err -> err.getPropertyPath() + ": " + err.getMessage())
                .findFirst()
                .orElse("Invalid parameter");
        return buildResponse(DEFAULT_STATUS, errorMsg);
    }

    //
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ExceptionMessage> handlerMethodValidationException(final HandlerMethodValidationException ex) {
        String errorMsg = ex.getParameterValidationResults()
                .stream()
                .flatMap(r -> r.getResolvableErrors().stream())
                .map(err -> err.getDefaultMessage() != null ? err.getDefaultMessage() : "Invalid parameter")
                .findFirst()
                .orElse("Invalid parameter");
        return buildResponse(DEFAULT_STATUS, errorMsg);
    }

    //  501 Not Implemented (webhook/provider chưa hỗ trợ)
    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ExceptionMessage> handlerUnsupported(final UnsupportedOperationException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Not implemented";
        return buildResponse(HttpStatus.NOT_IMPLEMENTED, msg);
    }

    //  403 Forbidden (Spring Security 6+: AuthorizationDeniedException từ @PreAuthorize)
    @ExceptionHandler({
            org.springframework.security.access.AccessDeniedException.class,
            org.springframework.security.authorization.AuthorizationDeniedException.class
    })
    public ResponseEntity<ExceptionMessage> handlerAccessDenied(final Exception ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "You don't have permission to access this resource");
    }

    //  405 Method Not Allowed
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ExceptionMessage> handlerMethodNotSupported(
            final org.springframework.web.HttpRequestMethodNotSupportedException ex) {
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage());
    }

    //  400 Missing param / type mismatch
    @ExceptionHandler({
            org.springframework.web.bind.MissingServletRequestParameterException.class,
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ExceptionMessage> handlerBadParam(final Exception ex) {
        return buildResponse(DEFAULT_STATUS, ex.getMessage());
    }

    //  503 Redis chết: fail-closed cho write (read đã fallback DB ở service)
    @ExceptionHandler({
            org.springframework.data.redis.RedisConnectionFailureException.class,
            org.springframework.dao.QueryTimeoutException.class
    })
    public ResponseEntity<ExceptionMessage> handlerRedisDown(final Exception ex) {
        log.error("Redis unavailable", ex);
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "Service temporarily unavailable, please retry");
    }

    //  404 route không tồn tại (static resource fallback của Spring)
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ExceptionMessage> handlerNoResource(
            final org.springframework.web.servlet.resource.NoResourceFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Not found");
    }

    //  400 body JSON sai (enum lạ, kiểu sai, thiếu field)
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionMessage> handlerNotReadable(
            final org.springframework.http.converter.HttpMessageNotReadableException ex) {
        log.error("Malformed request body", ex);
        return buildResponse(DEFAULT_STATUS, "Malformed request body");
    }

    //  409 ghi đồng thời cùng dòng (hết retry optimistic-lock) — client retry lại là xong
    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ExceptionMessage> handlerOptimisticLock(
            final org.springframework.orm.ObjectOptimisticLockingFailureException ex) {
        log.warn("Concurrent write conflict", ex);
        return buildResponse(HttpStatus.CONFLICT, "Conflict, please retry");
    }

    //  400 vi phạm ràng buộc DB (FK/unique) — thường do reference id không tồn tại;
    // log full stack vì lỗi gốc hay bị bọc nhiều lớp (flush sau exception)
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ExceptionMessage> handlerDataIntegrity(
            final org.springframework.dao.DataIntegrityViolationException ex) {
        log.error("Data integrity violation", ex);
        return buildResponse(DEFAULT_STATUS, "Invalid request: referenced data does not exist");
    }

    //  500 Error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionMessage> handleAllException(final Exception ex) {
        log.error("Unhandled exception", ex);
        String errorMsg = "Internal server error";
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, errorMsg);
    }

    private ResponseEntity<ExceptionMessage> buildResponse(HttpStatus status, String message) {
        ExceptionMessage response = ExceptionMessage
                .builder()
                .timestamp(ZonedDateTime.now())
                .status(status.value())
                .message(message)
                .build();
        return new ResponseEntity<>(response, status);
    }
}