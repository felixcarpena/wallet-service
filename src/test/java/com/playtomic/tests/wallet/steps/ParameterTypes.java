package com.playtomic.tests.wallet.steps;

import com.playtomic.tests.wallet.domain.Money;
import io.cucumber.java.ParameterType;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

public class ParameterTypes {

    @ParameterType("(\\d+(?:\\.\\d+)?) ([A-Z]{3})")
    public Money money(String amount, String currency) {
        return new Money(new BigDecimal(amount), Currency.getInstance(currency));
    }

    @ParameterType("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
    public UUID uuid(String value) {
        return UUID.fromString(value);
    }
}
