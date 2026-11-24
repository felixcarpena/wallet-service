package com.playtomic.tests.wallet.domain.exceptions;

import java.math.BigDecimal;

public class TooManyDecimalsException extends RuntimeException {

    public TooManyDecimalsException(BigDecimal amount, int allowedDecimals) {
        super("The amount " + amount.stripTrailingZeros().toPlainString() + " has more decimals than allowed (" + allowedDecimals + ")");
    }
}
