package com.playtomic.tests.wallet.infrastructure.persistence;

import com.playtomic.tests.wallet.domain.Wallet;
import com.playtomic.tests.wallet.domain.WalletRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

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
}
