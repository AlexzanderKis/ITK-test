package org.example.itktest.exception;

import java.util.UUID;

public class WalletNotFoundException extends RuntimeException {
    public WalletNotFoundException() {
        super("?WalletNotFound?");
    }

    // для сообщения
    public WalletNotFoundException(String string){
        super(string);
    }

    public WalletNotFoundException(UUID walletId) {
        super("?WalletNotFound?" + walletId);
    }
}

