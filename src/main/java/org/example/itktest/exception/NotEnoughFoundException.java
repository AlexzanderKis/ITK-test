package org.example.itktest.exception;

public class NotEnoughFoundException extends RuntimeException {
    public NotEnoughFoundException() {
        super("Недостаточно средств на счёте");

    }public NotEnoughFoundException(String message) {
        super(message);
    }
}
