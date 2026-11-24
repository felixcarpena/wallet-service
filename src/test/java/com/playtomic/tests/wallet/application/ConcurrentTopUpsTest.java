package com.playtomic.tests.wallet.application;

import com.playtomic.tests.wallet.domain.CardNumber;
import com.playtomic.tests.wallet.domain.ChargeResult;
import com.playtomic.tests.wallet.domain.Money;
import com.playtomic.tests.wallet.domain.PaymentProvider;
import com.playtomic.tests.wallet.domain.PaymentReference;
import com.playtomic.tests.wallet.domain.TopUpId;
import com.playtomic.tests.wallet.domain.Wallet;
import com.playtomic.tests.wallet.domain.WalletId;
import com.playtomic.tests.wallet.domain.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class ConcurrentTopUpsTest {

    private static final WalletId WALLET = new WalletId(UUID.fromString("00000000-0000-7000-8000-0000000000c1"));
    private static final CardNumber CARD = new CardNumber("4242 4242 4242 4242");
    private static final Currency EUR = Currency.getInstance("EUR");
    private static final int PLAYERS = 20;

    @Autowired
    private TopUpWallet topUpWallet;

    @Autowired
    private WalletReader walletReader;

    @Autowired
    private WalletRepository wallets;

    @Autowired
    private TransactionTemplate transactions;

    @Autowired
    private JdbcTemplate jdbc;

    @MockBean
    private PaymentProvider paymentProvider;

    @BeforeEach
    void an_empty_wallet_and_a_provider_that_accepts_every_charge() {
        JdbcTestUtils.deleteFromTables(jdbc, "wallet_movement", "top_up", "wallet");
        transactions.executeWithoutResult(status -> wallets.save(new Wallet(WALLET, eur("0.00"))));
        when(paymentProvider.charge(any(), any())).thenReturn(new ChargeResult.Charged(new PaymentReference("payment-1")));
    }

    @Test
    void top_ups_at_the_same_time_on_one_wallet_are_all_credited() throws Exception {
        topUpAllAtOnce(PLAYERS, eur("10.00"));

        assertThat(walletReader.findOrFail(WALLET).balance()).isEqualTo(eur("200.00"));
        assertThat(movementsOfTheWallet()).isEqualTo(PLAYERS);
    }

    private void topUpAllAtOnce(int players, Money amount) throws Exception {
        List<Callable<Void>> topUps = Collections.nCopies(players, () -> {
            topUpWallet.topUp(TopUpId.generate(), WALLET, amount, CARD);
            return null;
        });
        ExecutorService executor = Executors.newFixedThreadPool(players);
        try {
            for (Future<Void> topUp : executor.invokeAll(topUps)) {
                topUp.get();
            }
        } finally {
            executor.shutdown();
        }
    }

    private int movementsOfTheWallet() {
        return transactions.execute(status -> wallets.findOrFail(WALLET).movements().size());
    }

    private static Money eur(String amount) {
        return new Money(new BigDecimal(amount), EUR);
    }
}
