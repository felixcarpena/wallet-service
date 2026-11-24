package com.playtomic.tests.wallet.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CardNumberTest {

    @Test
    void shows_only_the_masked_number_when_printed() {
        assertThat(new CardNumber("4242 4242 4242 1234").toString()).isEqualTo("CardNumber[**** 1234]");
    }

    @Test
    void masks_all_but_the_last_four_digits() {
        assertThat(new CardNumber("4242 4242 4242 1234").masked()).isEqualTo("**** 1234");
    }
}
