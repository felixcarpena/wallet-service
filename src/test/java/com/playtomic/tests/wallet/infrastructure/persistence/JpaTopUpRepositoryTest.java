package com.playtomic.tests.wallet.infrastructure.persistence;

import com.playtomic.tests.wallet.domain.CardNumber;
import com.playtomic.tests.wallet.domain.ChargeResult;
import com.playtomic.tests.wallet.domain.Money;
import com.playtomic.tests.wallet.domain.PaymentReference;
import com.playtomic.tests.wallet.domain.TopUp;
import com.playtomic.tests.wallet.domain.TopUpId;
import com.playtomic.tests.wallet.domain.TopUpStatus;
import com.playtomic.tests.wallet.domain.WalletId;
import com.playtomic.tests.wallet.domain.exceptions.TopUpNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(JpaTopUpRepository.class)
class JpaTopUpRepositoryTest {

    private static final WalletId WALLET = new WalletId(UUID.fromString("00000000-0000-7000-8000-000000000001"));
    private static final Money AMOUNT = new Money(new BigDecimal("15.00"), Currency.getInstance("EUR"));

    @Autowired
    private JpaTopUpRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void finds_a_saved_top_up_locked_for_update() {
        TopUp topUp = saved(pendingTopUp());

        TopUp found = repository.findForUpdateOrFail(topUp.id());

        assertThat(found.status()).isEqualTo(TopUpStatus.PENDING);
        assertThat(entityManager.getLockMode(found)).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }

    @Test
    void finds_a_completed_top_up_with_its_payment_reference() {
        TopUp topUp = pendingTopUp();
        topUp.on(new ChargeResult.Charged(new PaymentReference("payment-1")));
        topUp.walletCredited();
        saved(topUp);

        TopUp found = repository.findForUpdateOrFail(topUp.id());

        assertThat(found.status()).isEqualTo(TopUpStatus.COMPLETED);
        assertThat(found.paymentReference()).contains(new PaymentReference("payment-1"));
    }

    @Test
    void rejects_an_unknown_top_up() {
        assertThatThrownBy(() -> repository.findForUpdateOrFail(TopUpId.generate())).isInstanceOf(TopUpNotFoundException.class);
    }

    private TopUp saved(TopUp topUp) {
        repository.save(topUp);
        entityManager.flush();
        entityManager.clear();
        return topUp;
    }

    private static TopUp pendingTopUp() {
        return TopUp.start(TopUpId.generate(), WALLET, AMOUNT, new CardNumber("4242 4242 4242 4242"));
    }
}
