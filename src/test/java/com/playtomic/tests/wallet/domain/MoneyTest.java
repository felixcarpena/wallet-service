package com.playtomic.tests.wallet.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyTest {

    private static final Currency EUR = Currency.getInstance("EUR");

    @Test
    void amounts_with_different_written_scale_are_equal() {
        assertThat(new Money(new BigDecimal("25"), EUR)).isEqualTo(new Money(new BigDecimal("25.00"), EUR));
    }

    @Test
    void keeps_four_decimals_internally() {
        assertThat(new Money(new BigDecimal("25.5"), EUR).amount()).isEqualTo(new BigDecimal("25.5000"));
    }

}
