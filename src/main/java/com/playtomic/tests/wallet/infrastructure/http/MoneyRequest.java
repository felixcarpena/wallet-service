package com.playtomic.tests.wallet.infrastructure.http;

import com.playtomic.tests.wallet.domain.Money;

import java.math.BigDecimal;
import java.util.Currency;

public record MoneyRequest(BigDecimal amount, String currency) {

    Money toMoney() {
        return new Money(amount, Currency.getInstance(currency));
    }
}
