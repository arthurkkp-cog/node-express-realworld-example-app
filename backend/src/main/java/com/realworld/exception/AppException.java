package com.realworld.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

@Getter
public class AppException extends RuntimeException {
    private final HttpStatus status;
    private final Map<String, List<String>> errors;

    public AppException(HttpStatus status, Map<String, List<String>> errors) {
        super("Application Exception");
        this.status = status;
        this.errors = errors;
    }

    public AppException(HttpStatus status, String field, String message) {
        super(message);
        this.status = status;
        this.errors = Map.of(field, List.of(message));
    }
}
