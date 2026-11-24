package com.playtomic.tests.wallet.infrastructure.persistence;

import com.playtomic.tests.wallet.domain.Wallet;
import com.playtomic.tests.wallet.domain.WalletId;
import com.playtomic.tests.wallet.domain.WalletRepository;
import com.playtomic.tests.wallet.domain.exceptions.WalletNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaWalletRepository implements WalletRepository {

    private final EntityManager entityManager;

    public JpaWalletRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void save(Wallet wallet) {
        entityManager.persist(wallet);
    }

    @Override
    public Wallet findForUpdateOrFail(WalletId id) throws WalletNotFoundException {
        return Optional.ofNullable(entityManager.find(Wallet.class, id.value(), LockModeType.PESSIMISTIC_WRITE))
                .orElseThrow(() -> new WalletNotFoundException(id));
    }
}
