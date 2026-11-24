package com.playtomic.tests.wallet.infrastructure.persistence;

import com.playtomic.tests.wallet.domain.TopUp;
import com.playtomic.tests.wallet.domain.TopUpId;
import com.playtomic.tests.wallet.domain.TopUpRepository;
import com.playtomic.tests.wallet.domain.exceptions.TopUpNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JpaTopUpRepository implements TopUpRepository {

    private final EntityManager entityManager;

    public JpaTopUpRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void save(TopUp topUp) {
        entityManager.persist(topUp);
    }

    @Override
    public TopUp findForUpdateOrFail(TopUpId id) throws TopUpNotFoundException {
        return Optional.ofNullable(entityManager.find(TopUp.class, id.value(), LockModeType.PESSIMISTIC_WRITE))
                .orElseThrow(() -> new TopUpNotFoundException(id));
    }
}
