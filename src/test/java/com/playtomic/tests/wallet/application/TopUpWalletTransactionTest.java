package com.playtomic.tests.wallet.application;

import com.playtomic.tests.wallet.domain.CardNumber;
import com.playtomic.tests.wallet.domain.ChargeResult;
import com.playtomic.tests.wallet.domain.Money;
import com.playtomic.tests.wallet.domain.PaymentProvider;
import com.playtomic.tests.wallet.domain.PaymentReference;
import com.playtomic.tests.wallet.domain.TopUp;
import com.playtomic.tests.wallet.domain.TopUpId;
import com.playtomic.tests.wallet.domain.TopUpRepository;
import com.playtomic.tests.wallet.domain.TopUpStatus;
import com.playtomic.tests.wallet.domain.Wallet;
import com.playtomic.tests.wallet.domain.WalletId;
import com.playtomic.tests.wallet.domain.WalletRepository;
import jakarta.persistence.PessimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class TopUpWalletTransactionTest {

    private static final WalletId WALLET = new WalletId(UUID.fromString("00000000-0000-7000-8000-0000000000b1"));
    private static final TopUpId TOP_UP = new TopUpId(UUID.fromString("00000000-0000-7000-8000-0000000000b2"));
    private static final CardNumber CARD = new CardNumber("4242 4242 4242 4242");
    private static final Money AMOUNT = new Money(new BigDecimal("15.00"), Currency.getInstance("EUR"));
    private static final ChargeResult CHARGED = new ChargeResult.Charged(new PaymentReference("payment-1"));

    @Autowired
    private TopUpWallet topUpWallet;

    @Autowired
    private TopUpReader topUpReader;

    @SpyBean
    private TopUpRepository topUps;

    @SpyBean
    private WalletRepository wallets;

    @Autowired
    private TransactionTemplate transactions;

    @Autowired
    private JdbcTemplate jdbc;

    @MockBean
    private PaymentProvider paymentProvider;

    @BeforeEach
    void a_wallet_with_ten_euros() {
        JdbcTestUtils.deleteFromTables(jdbc, "wallet_movement", "top_up", "wallet");
        transactions.executeWithoutResult(status -> wallets.save(new Wallet(WALLET, new Money(BigDecimal.TEN, Currency.getInstance("EUR")))));
    }

    @Test
    void the_card_is_charged_with_no_transaction_open_once_the_pending_top_up_is_committed() {
        AtomicBoolean transactionOpenWhileCharging = new AtomicBoolean(true);
        AtomicReference<TopUpStatus> statusSeenWhileCharging = new AtomicReference<>();
        when(paymentProvider.charge(any(), any())).thenAnswer(invocation -> {
            transactionOpenWhileCharging.set(TransactionSynchronizationManager.isActualTransactionActive());
            statusSeenWhileCharging.set(topUpReader.findOrFail(TOP_UP).status());
            return CHARGED;
        });

        topUpWallet.topUp(TOP_UP, WALLET, AMOUNT, CARD);

        assertThat(transactionOpenWhileCharging).isFalse();
        assertThat(statusSeenWhileCharging).hasValue(TopUpStatus.PENDING);
        assertThat(topUpReader.findOrFail(TOP_UP).status()).isEqualTo(TopUpStatus.COMPLETED);
    }

    @Test
    void a_charged_top_up_stays_charged_with_its_payment_reference_when_the_wallet_cannot_be_credited() {
        when(paymentProvider.charge(any(), any())).thenReturn(CHARGED);
        doThrow(new PessimisticLockException("wallet locked")).when(wallets).findForUpdateOrFail(WALLET);

        assertThatThrownBy(() -> topUpWallet.topUp(TOP_UP, WALLET, AMOUNT, CARD)).isInstanceOf(PessimisticLockingFailureException.class);

        TopUp stored = transactions.execute(status -> topUps.findForUpdateOrFail(TOP_UP));
        assertThat(stored.status()).isEqualTo(TopUpStatus.CHARGED);
        assertThat(stored.paymentReference()).contains(new PaymentReference("payment-1"));
        doCallRealMethod().when(wallets).findForUpdateOrFail(WALLET);
        Wallet wallet = transactions.execute(status -> {
            Wallet found = wallets.findForUpdateOrFail(WALLET);
            found.movements().size();
            return found;
        });
        assertThat(wallet.balance()).isEqualTo(new Money(BigDecimal.TEN, Currency.getInstance("EUR")));
        assertThat(wallet.movements()).isEmpty();
    }

    @Test
    void the_top_up_is_locked_before_the_wallet() {
        when(paymentProvider.charge(any(), any())).thenReturn(CHARGED);

        topUpWallet.topUp(TOP_UP, WALLET, AMOUNT, CARD);

        InOrder order = inOrder(topUps, wallets);
        order.verify(topUps, times(2)).findForUpdateOrFail(TOP_UP);
        order.verify(wallets).findForUpdateOrFail(WALLET);
    }
}
