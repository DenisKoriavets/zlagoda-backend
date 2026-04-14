package ua.edu.ukma.zlagodabackend.exception;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ua.edu.ukma.zlagodabackend.dto.error.ErrorResponse;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request
    ) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (ObjectError error : ex.getBindingResult().getAllErrors()) {
            String fieldName = (error instanceof FieldError fe) ? fe.getField() : error.getObjectName();
            errors.put(fieldName, error.getDefaultMessage());
        }
        return buildResponse(HttpStatus.BAD_REQUEST, "Помилка валідації вхідних даних", errors);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Business logic error: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<Object> handleBusinessValidation(BusinessValidationException ex) {
        log.warn("Business validation error: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<Object> handleInsufficientStock(InsufficientStockException ex) {
        log.warn("Insufficient stock: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("Data Integrity Violation: {}", ex.getMessage());
        String message = "Операцію неможливо виконати через пов’язані записи в базі даних.";
        String m = ex.getMessage() != null ? ex.getMessage() : "";
        if (m.contains("store_product_upc_prom_fkey")) {
            message = "Поле «Звичайний UPC» має бути порожнім або вказувати на наявну позицію в магазині.";
        } else if (m.contains("product_category_number_fkey")) {
            message = "Неможливо видалити категорію: у каталозі є товари цієї категорії.";
        } else if (m.contains("store_product_id_product_fkey")) {
            message = "Неможливо видалити товар: у магазині є позиції (UPC) цього товару.";
        } else if (m.contains("sale_upc_fkey")) {
            message = "Неможливо видалити позицію: цей UPC є в історії продажів.";
        } else if (m.contains("check_id_employee_fkey")) {
            message = "Неможливо видалити працівника: є чеки, оформлені цим працівником.";
        } else if (m.contains("check_card_number_fkey")) {
            message = "Неможливо видалити карту клієнта: є чеки з цією карткою.";
        }
        return buildResponse(HttpStatus.CONFLICT, message);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Object> handleDatabaseExceptions(DataAccessException ex) {
        log.error("Database Error: ", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Внутрішня помилка бази даних");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Bad credentials: {}", ex.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "Невірний ID або пароль");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex) {
        log.error("Unexpected error", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Невідома помилка сервера");
    }

    private ResponseEntity<Object> buildResponse(HttpStatus status, String message, Map<String, String> errors) {
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                errors
        );
        return new ResponseEntity<>(response, status);
    }

    private ResponseEntity<Object> buildResponse(HttpStatus status, String message) {
        return buildResponse(status, message, null);
    }
}