package com.playtomic.tests.wallet.infrastructure.persistence;

import com.playtomic.tests.wallet.domain.Money;
import com.playtomic.tests.wallet.domain.TopUpId;
import com.playtomic.tests.wallet.domain.Wallet;
import com.playtomic.tests.wallet.domain.WalletId;
import com.playtomic.tests.wallet.domain.WalletMovementType;
import com.playtomic.tests.wallet.domain.exceptions.WalletNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaWalletRepository.class)
class JpaWalletRepositoryTest {

    private static final WalletId WALLET = new WalletId(UUID.fromString("00000000-0000-7000-8000-000000000001"));

    @Autowired
    private JpaWalletRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void finds_a_saved_wallet_locked_for_update() {
        saved(new Wallet(WALLET, eur("10.00")));

        Wallet found = repository.findForUpdateOrFail(WALLET);

        assertThat(found.balance()).isEqualTo(eur("10.00"));
        assertThat(entityManager.getLockMode(found)).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }

    @Test
    void finds_a_saved_wallet_without_locking_it() {
        saved(new Wallet(WALLET, eur("10.00")));

        Wallet found = repository.findOrFail(WALLET);

        assertThat(found.balance()).isEqualTo(eur("10.00"));
        assertThat(entityManager.getLockMode(found)).isNotEqualTo(LockModeType.PESSIMISTIC_WRITE);
    }

    @Test
    void rejects_an_unknown_wallet() {
        assertThatThrownBy(() -> repository.findOrFail(WALLET)).isInstanceOf(WalletNotFoundException.class);
        assertThatThrownBy(() -> repository.findForUpdateOrFail(WALLET)).isInstanceOf(WalletNotFoundException.class);
    }

    @Test
    void finds_a_credited_wallet_with_its_balance_and_movement() {
        TopUpId topUpId = TopUpId.generate();
        Wallet wallet = new Wallet(WALLET, eur("10.00"));
        wallet.credit(eur("15.00"), topUpId);
        saved(wallet);

        Wallet found = repository.findForUpdateOrFail(WALLET);

        assertThat(found.balance()).isEqualTo(eur("25.00"));
        assertThat(found.movements()).singleElement().satisfies(movement -> {
            assertThat(movement.type()).isEqualTo(WalletMovementType.TOP_UP);
            assertThat(movement.amount()).isEqualTo(eur("15.00"));
            assertThat(movement.causeId()).isEqualTo(topUpId.value());
        });
    }

    @Test
    void a_found_wallet_does_not_load_its_movements() {
        Wallet wallet = new Wallet(WALLET, eur("10.00"));
        wallet.credit(eur("15.00"), TopUpId.generate());
        saved(wallet);

        Wallet found = repository.findForUpdateOrFail(WALLET);

        assertThat(entityManager.getEntityManagerFactory().getPersistenceUnitUtil().isLoaded(found, "movements")).isFalse();
    }

    @Test
    void the_same_top_up_cannot_be_credited_twice() {
        TopUpId topUpId = TopUpId.generate();
        Wallet wallet = new Wallet(WALLET, eur("10.00"));
        wallet.credit(eur("15.00"), topUpId);
        wallet.credit(eur("15.00"), topUpId);
        repository.save(wallet);

        assertThatThrownBy(() -> entityManager.flush()).isInstanceOf(PersistenceException.class);
    }

    private void saved(Wallet wallet) {
        repository.save(wallet);
        entityManager.flush();
        entityManager.clear();
    }

    private static Money eur(String amount) {
        return new Money(new BigDecimal(amount), Currency.getInstance("EUR"));
    }
}
