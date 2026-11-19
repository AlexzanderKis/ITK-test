package org.example.itktest.exception;

import java.util.UUID;

public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException() {
        super("Счёт не найден");
    }

    // для сообщения
    public WalletNotFoundException(String string){
        super(string);
    }

    public WalletNotFoundException(UUID walletId) {
        super("Счёт " + walletId+" не найден");
    }
}

