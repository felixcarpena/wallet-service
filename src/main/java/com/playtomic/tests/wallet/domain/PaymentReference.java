package com.playtomic.tests.wallet.domain;

import java.util.Objects;

public record PaymentReference(String value) {

    public PaymentReference {
        Objects.requireNonNull(value, "value");
    }
}
