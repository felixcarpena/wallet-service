package com.playtomic.tests.wallet.domain;

import com.playtomic.tests.wallet.domain.exceptions.CurrencyMismatchException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void amounts_with_different_written_scale_are_equal() {
        assertThat(new Money(new BigDecimal("25"), EUR)).isEqualTo(new Money(new BigDecimal("25.00"), EUR));
    }

    @Test
    void keeps_four_decimals_internally() {
        assertThat(new Money(new BigDecimal("25.5"), EUR).amount()).isEqualTo(new BigDecimal("25.5000"));
    }

    @Test
    void adds_amounts_in_the_same_currency() {
        assertThat(eur("10.00").add(eur("15.00"))).isEqualTo(eur("25.00"));
    }

    @Test
    void refuses_to_add_amounts_in_different_currencies() {
        assertThatThrownBy(() -> eur("10.00").add(new Money(BigDecimal.ONE, USD)))
                .isInstanceOf(CurrencyMismatchException.class);
    }

    @Test
    void is_positive_only_above_zero() {
        assertThat(eur("0.01").isPositive()).isTrue();
        assertThat(eur("0.00").isPositive()).isFalse();
        assertThat(eur("-1.00").isPositive()).isFalse();
    }

    private static Money eur(String amount) {
        return new Money(new BigDecimal(amount), EUR);
    }
}
