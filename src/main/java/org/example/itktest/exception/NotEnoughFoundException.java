package org.example.itktest.exception;

public class NotEnoughFoundException extends RuntimeException {
    public NotEnoughFoundException() {
        super("?NotEnoughFoundsOnBalance?");

    }public NotEnoughFoundException(String message) {
        super(message);
    }
}
