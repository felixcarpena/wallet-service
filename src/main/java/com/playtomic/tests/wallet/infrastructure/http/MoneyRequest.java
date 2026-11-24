package com.playtomic.tests.wallet.infrastructure.http;

import com.playtomic.tests.wallet.domain.Money;
import com.playtomic.tests.wallet.infrastructure.http.exceptions.InvalidTopUpRequestException;

import java.math.BigDecimal;
import java.util.Currency;

public record MoneyRequest(BigDecimal amount, String currency) {

    Money toMoney() {
        if (amount == null || currency == null) {
            throw new InvalidTopUpRequestException("An amount needs a value and a currency");
        }
        try {
            return new Money(amount, Currency.getInstance(currency));
        } catch (IllegalArgumentException unknownCurrency) {
            throw new InvalidTopUpRequestException("Unknown currency " + currency);
        }
    }
}
