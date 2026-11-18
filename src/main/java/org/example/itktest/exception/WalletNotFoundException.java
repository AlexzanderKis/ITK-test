package org.example.itktest.exception;

public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException() {
        super("?WalletNotFound?");
    }

    // для сообщения
    public WalletNotFoundException(String string){
        super(string);
    }
}

