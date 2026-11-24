package com.playtomic.tests.wallet.domain;

import com.playtomic.tests.wallet.domain.exceptions.IllegalTopUpTransitionException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TopUpTest {

    private static final TopUpId TOP_UP = new TopUpId(UUID.fromString("00000000-0000-7000-8000-0000000000a1"));
    private static final WalletId WALLET = new WalletId(UUID.fromString("00000000-0000-7000-8000-000000000001"));
    private static final Money AMOUNT = new Money(new BigDecimal("15.00"), Currency.getInstance("EUR"));
    private static final CardNumber CARD = new CardNumber("4242 4242 4242 4242");
    private static final ChargeResult CHARGED = new ChargeResult.Charged(new PaymentReference("payment-1"));

    @Test
    void a_started_top_up_is_pending() {
        assertThat(TopUp.start(TOP_UP, WALLET, AMOUNT, CARD).status()).isEqualTo(TopUpStatus.PENDING);
    }

    @Test
    void a_charged_card_moves_it_to_charged_and_asks_to_credit_the_wallet() {
        TopUp topUp = TopUp.start(TOP_UP, WALLET, AMOUNT, CARD);

        CreditWallet creditWallet = topUp.on(CHARGED);

        assertThat(creditWallet).isEqualTo(new CreditWallet(WALLET, AMOUNT, TOP_UP));
        assertThat(topUp.status()).isEqualTo(TopUpStatus.CHARGED);
        assertThat(topUp.paymentReference()).contains(new PaymentReference("payment-1"));
    }

    @Test
    void crediting_the_wallet_completes_it() {
        TopUp topUp = TopUp.start(TOP_UP, WALLET, AMOUNT, CARD);
        topUp.on(CHARGED);

        topUp.walletCredited();

        assertThat(topUp.status()).isEqualTo(TopUpStatus.COMPLETED);
    }

    @Test
    void a_wallet_cannot_be_credited_before_the_card_is_charged() {
        TopUp topUp = TopUp.start(TOP_UP, WALLET, AMOUNT, CARD);

        assertThatThrownBy(topUp::walletCredited)
                .isInstanceOf(IllegalTopUpTransitionException.class)
                .hasMessageContaining("must be CHARGED");
    }

    @Test
    void a_charge_is_applied_only_once() {
        TopUp topUp = TopUp.start(TOP_UP, WALLET, AMOUNT, CARD);
        topUp.on(CHARGED);

        assertThatThrownBy(() -> topUp.on(CHARGED))
                .isInstanceOf(IllegalTopUpTransitionException.class)
                .hasMessageContaining("must be PENDING");
    }
}
