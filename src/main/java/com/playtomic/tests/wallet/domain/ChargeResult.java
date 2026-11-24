package com.playtomic.tests.wallet.domain;

public sealed interface ChargeResult {

    record Charged(PaymentReference reference) implements ChargeResult {
    }

    record Declined() implements ChargeResult {
    }

    record Failed() implements ChargeResult {
    }

    record Unconfirmed() implements ChargeResult {
    }
}
