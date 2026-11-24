package com.playtomic.tests.wallet.domain.exceptions;

public class InvalidCardNumberException extends RuntimeException {

    public InvalidCardNumberException() {
        super("A card number is required");
    }
}
