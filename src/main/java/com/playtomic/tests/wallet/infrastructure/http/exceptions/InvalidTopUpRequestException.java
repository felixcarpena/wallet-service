package com.playtomic.tests.wallet.infrastructure.http.exceptions;

public class InvalidTopUpRequestException extends RuntimeException {

    public InvalidTopUpRequestException(String message) {
        super(message);
    }
}
