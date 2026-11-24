package com.playtomic.tests.wallet.infrastructure.http;

public record TopUpRequest(MoneyRequest amount, String cardNumber) {
}
