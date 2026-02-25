/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.exception;

import org.springframework.http.HttpStatus;

public class ResponseException extends RuntimeException {
    final  private HttpStatus status;
    public ResponseException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
