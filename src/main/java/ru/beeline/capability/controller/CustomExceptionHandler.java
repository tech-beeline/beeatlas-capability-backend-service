package ru.beeline.capability.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.beeline.capability.exception.*;

@ControllerAdvice
@Slf4j
public class CustomExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleException(IllegalArgumentException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("400 BAD_REQUEST : " + e.getMessage());
    }

    @ExceptionHandler(ResponseException.class)
    public ResponseEntity<Object> handleException(ResponseException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(e.getStatus()).body(e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleException(NotFoundException e) {
        log.error(e.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .header("content-type", MediaType.APPLICATION_JSON_VALUE)
                .body(e.getMessage());
    }

    @ExceptionHandler(PackageRegistrationException.class)
    public ResponseEntity<Object> handleException(PackageRegistrationException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body("409 CONFLICT : " + e.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Object> handleException(ForbiddenException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("403 FORBIDDEN : " + e.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handleException(ValidationException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body("409 Ошибка валидации тела запроса : " + e.getMessage());
    }

    @ExceptionHandler(TooManyResultsException.class)
    public ResponseEntity<Object> handleException(TooManyResultsException e) {
        log.error(e.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("422 UNPROCESSABLE_ENTITY : " + e.getMessage());
    }

    @ExceptionHandler(DocumentServerException.class)
    public ResponseEntity<Object> handleException(DocumentServerException e) {
        log.error(e.getMessage());
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .header("content-type", MediaType.APPLICATION_JSON_VALUE)
                .body(e.getMessage());
    }
}