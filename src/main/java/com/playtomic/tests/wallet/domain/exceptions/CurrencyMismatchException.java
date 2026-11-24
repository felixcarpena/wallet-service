package com.playtomic.tests.wallet.domain.exceptions;

import java.util.Currency;

public class CurrencyMismatchException extends RuntimeException {

    public CurrencyMismatchException(Currency expected, Currency actual) {
        super("Expected an amount in " + expected + " but got " + actual);
    }
}
