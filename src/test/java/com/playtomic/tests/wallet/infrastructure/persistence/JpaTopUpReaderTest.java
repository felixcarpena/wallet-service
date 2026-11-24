package com.playtomic.tests.wallet.infrastructure.persistence;

import com.playtomic.tests.wallet.application.TopUpView;
import com.playtomic.tests.wallet.domain.CardNumber;
import com.playtomic.tests.wallet.domain.Money;
import com.playtomic.tests.wallet.domain.TopUp;
import com.playtomic.tests.wallet.domain.TopUpId;
import com.playtomic.tests.wallet.domain.TopUpStatus;
import com.playtomic.tests.wallet.domain.WalletId;
import com.playtomic.tests.wallet.domain.exceptions.TopUpNotFoundException;
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
@Import({JpaTopUpRepository.class, JpaTopUpReader.class})
class JpaTopUpReaderTest {

    private static final TopUpId TOP_UP = new TopUpId(UUID.fromString("00000000-0000-7000-8000-0000000000a1"));
    private static final WalletId WALLET = new WalletId(UUID.fromString("00000000-0000-7000-8000-000000000001"));
    private static final Money AMOUNT = new Money(new BigDecimal("15.12"), Currency.getInstance("EUR"));

    @Autowired
    private JpaTopUpRepository repository;

    @Autowired
    private JpaTopUpReader reader;

    @Autowired
    private EntityManager entityManager;

    @Test
    void reads_a_saved_top_up_as_a_view() {
        repository.save(TopUp.start(TOP_UP, WALLET, AMOUNT, new CardNumber("4242 4242 4242 4242")));
        entityManager.flush();
        entityManager.clear();

        assertThat(reader.findOrFail(TOP_UP)).isEqualTo(new TopUpView(TOP_UP, TopUpStatus.PENDING, AMOUNT));
    }

    @Test
    void rejects_an_unknown_top_up() {
        assertThatThrownBy(() -> reader.findOrFail(TOP_UP)).isInstanceOf(TopUpNotFoundException.class);
    }
}
