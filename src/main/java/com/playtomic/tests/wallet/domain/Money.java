package com.playtomic.tests.wallet.domain;

import com.playtomic.tests.wallet.domain.exceptions.CurrencyMismatchException;
import com.playtomic.tests.wallet.domain.exceptions.TooManyDecimalsException;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

@Embeddable
public record Money(BigDecimal amount, Currency currency) {

    private static final int SCALE = 4;

    public Money {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(currency, "currency");
        if (amount.stripTrailingZeros().scale() > SCALE) {
            throw new TooManyDecimalsException(amount, SCALE);
        }
        amount = amount.setScale(SCALE, RoundingMode.UNNECESSARY);
    }

    public Money add(Money other) {
        if (!currency.equals(other.currency)) {
            throw new CurrencyMismatchException(currency, other.currency);
        }
        return new Money(amount.add(other.amount), currency);
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }
}
