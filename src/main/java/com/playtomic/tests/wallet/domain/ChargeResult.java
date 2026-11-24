package com.playtomic.tests.wallet.domain;

public sealed interface ChargeResult {

    record Charged(PaymentReference reference) implements ChargeResult {
    }
}
