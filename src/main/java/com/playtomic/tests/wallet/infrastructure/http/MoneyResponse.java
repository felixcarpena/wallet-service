package com.playtomic.tests.wallet.infrastructure.http;

import com.playtomic.tests.wallet.domain.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record MoneyResponse(BigDecimal amount, String currency) {

    static MoneyResponse from(Money money) {
        int decimals = money.currency().getDefaultFractionDigits();
        return new MoneyResponse(money.amount().setScale(decimals, RoundingMode.HALF_EVEN), money.currency().getCurrencyCode());
    }
}
