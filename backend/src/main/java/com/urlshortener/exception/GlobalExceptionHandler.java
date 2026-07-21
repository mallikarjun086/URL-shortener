package com.urlshortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @SuppressWarnings("null")
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Invalid argument";
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, msg);
        problemDetail.setType(Objects.requireNonNull(URI.create("https://api.shortener.com/errors/bad-request")));
        problemDetail.setTitle("Bad Request");
        return problemDetail;
    }

    @ExceptionHandler(IllegalStateException.class)
    @SuppressWarnings("null")
    public ProblemDetail handleIllegalState(IllegalStateException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Link expired";
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.GONE, msg);
        problemDetail.setType(Objects.requireNonNull(URI.create("https://api.shortener.com/errors/link-expired")));
        problemDetail.setTitle("Link Expired");
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @SuppressWarnings("null")
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + ", " + b)
                .orElse("Validation failed");

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        problemDetail.setType(Objects.requireNonNull(URI.create("https://api.shortener.com/errors/validation-failed")));
        problemDetail.setTitle("Validation Failed");
        return problemDetail;
    }
}
