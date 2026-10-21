package br.com.fatec.venda.controller;

import br.com.fatec.venda.controller.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(IllegalArgumentException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex, request);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleConflict(IllegalStateException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex, request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, Exception ex, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(), request.getRequestURI(), status.value(), status.getReasonPhrase(), ex.getMessage());
        return ResponseEntity.status(status).body(body);
    }
}
