package ru.beeline.capability.exception;

public class TooManyResultsException extends RuntimeException {
    public TooManyResultsException(String message) {
        super(message);
    }
}
