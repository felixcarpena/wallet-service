package com.playtomic.tests.wallet.domain.exceptions;

import com.playtomic.tests.wallet.domain.Money;

public class NonPositiveAmountException extends RuntimeException {

    public NonPositiveAmountException(Money amount) {
        super("Expected a positive amount but got " + amount.amount() + " " + amount.currency());
    }
}
