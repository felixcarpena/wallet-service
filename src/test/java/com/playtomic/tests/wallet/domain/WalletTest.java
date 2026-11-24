package com.playtomic.tests.wallet.domain;

import com.playtomic.tests.wallet.domain.exceptions.CurrencyMismatchException;
import com.playtomic.tests.wallet.domain.exceptions.NonPositiveAmountException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WalletTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final WalletId WALLET = new WalletId(UUID.fromString("00000000-0000-7000-8000-000000000001"));
    private static final TopUpId TOP_UP = new TopUpId(UUID.fromString("00000000-0000-7000-8000-0000000000a1"));

    @Test
    void a_credit_adds_the_amount_to_the_balance() {
        Wallet wallet = new Wallet(WALLET, eur("10.00"));

        wallet.credit(eur("15.00"), TOP_UP);

        assertThat(wallet.balance()).isEqualTo(eur("25.00"));
    }

    @Test
    void a_credit_records_a_top_up_movement_with_a_positive_amount() {
        Wallet wallet = new Wallet(WALLET, eur("10.00"));

        WalletMovement movement = wallet.credit(eur("15.00"), TOP_UP);

        assertThat(movement.type()).isEqualTo(WalletMovementType.TOP_UP);
        assertThat(movement.amount()).isEqualTo(eur("15.00"));
        assertThat(movement.causeId()).isEqualTo(TOP_UP.value());
        assertThat(wallet.movements()).containsExactly(movement);
    }

    @Test
    void a_credit_must_be_positive() {
        Wallet wallet = new Wallet(WALLET, eur("10.00"));

        assertThatThrownBy(() -> wallet.credit(eur("0.00"), TOP_UP)).isInstanceOf(NonPositiveAmountException.class);
        assertThatThrownBy(() -> wallet.credit(eur("-5.00"), TOP_UP)).isInstanceOf(NonPositiveAmountException.class);
        assertThat(wallet.balance()).isEqualTo(eur("10.00"));
    }

    @Test
    void a_credit_must_be_in_the_wallet_currency() {
        Wallet wallet = new Wallet(WALLET, eur("10.00"));

        assertThatThrownBy(() -> wallet.credit(new Money(BigDecimal.TEN, Currency.getInstance("USD")), TOP_UP))
                .isInstanceOf(CurrencyMismatchException.class);
        assertThat(wallet.balance()).isEqualTo(eur("10.00"));
    }

    @Test
    void a_credit_can_be_validated_before_it_happens_without_changing_the_wallet() {
        Wallet wallet = new Wallet(WALLET, eur("10.00"));

        assertThatThrownBy(() -> wallet.validateCredit(eur("0.00"))).isInstanceOf(NonPositiveAmountException.class);
        assertThatThrownBy(() -> wallet.validateCredit(new Money(BigDecimal.TEN, Currency.getInstance("USD"))))
                .isInstanceOf(CurrencyMismatchException.class);
        wallet.validateCredit(eur("15.00"));
        assertThat(wallet.balance()).isEqualTo(eur("10.00"));
        assertThat(wallet.movements()).isEmpty();
    }

    private static Money eur(String amount) {
        return new Money(new BigDecimal(amount), EUR);
    }
}
