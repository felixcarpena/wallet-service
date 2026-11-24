package com.playtomic.tests.wallet.infrastructure.persistence;

import com.playtomic.tests.wallet.application.WalletView;
import com.playtomic.tests.wallet.domain.Money;
import com.playtomic.tests.wallet.domain.Wallet;
import com.playtomic.tests.wallet.domain.WalletId;
import com.playtomic.tests.wallet.domain.exceptions.WalletNotFoundException;
import jakarta.persistence.EntityManager;
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
@Import({JpaWalletRepository.class, JpaWalletReader.class})
class JpaWalletReaderTest {

    private static final WalletId WALLET = new WalletId(UUID.fromString("00000000-0000-7000-8000-000000000001"));
    private static final Money BALANCE = new Money(new BigDecimal("25.1234"), Currency.getInstance("EUR"));

    @Autowired
    private JpaWalletRepository repository;

    @Autowired
    private JpaWalletReader reader;

    @Autowired
    private EntityManager entityManager;

    @Test
    void reads_a_saved_wallet_as_a_view() {
        repository.save(new Wallet(WALLET, BALANCE));
        entityManager.flush();
        entityManager.clear();

        assertThat(reader.findOrFail(WALLET)).isEqualTo(new WalletView(WALLET, BALANCE));
    }

    @Test
    void rejects_an_unknown_wallet() {
        assertThatThrownBy(() -> reader.findOrFail(WALLET)).isInstanceOf(WalletNotFoundException.class);
    }
}
