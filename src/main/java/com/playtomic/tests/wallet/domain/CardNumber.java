package com.playtomic.tests.wallet.domain;

import com.playtomic.tests.wallet.domain.exceptions.InvalidCardNumberException;

public record CardNumber(String value) {

    public CardNumber {
        if (value == null || value.isBlank()) {
            throw new InvalidCardNumberException();
        }
    }

    public String masked() {
        String digits = value.replaceAll("\\s", "");
        return "**** " + digits.substring(Math.max(0, digits.length() - 4));
    }

    @Override
    public String toString() {
        return "CardNumber[" + masked() + "]";
    }
}
