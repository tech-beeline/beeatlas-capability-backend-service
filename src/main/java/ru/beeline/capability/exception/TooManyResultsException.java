/*
 * Copyright (c) 2024 PJSC VimpelCom
 */

package ru.beeline.capability.exception;

public class TooManyResultsException extends RuntimeException {
    public TooManyResultsException(String message) {
        super(message);
    }
}
