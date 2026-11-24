package com.playtomic.tests.wallet.domain;

import java.util.Objects;

public record CardNumber(String value) {

    public CardNumber {
        Objects.requireNonNull(value, "value");
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
