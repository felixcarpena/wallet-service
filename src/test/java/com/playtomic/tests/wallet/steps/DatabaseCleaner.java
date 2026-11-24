package com.playtomic.tests.wallet.steps;

import io.cucumber.java.Before;
import jakarta.persistence.EntityManager;
import org.springframework.transaction.support.TransactionTemplate;

public class DatabaseCleaner {

    private final EntityManager entityManager;
    private final TransactionTemplate transaction;

    public DatabaseCleaner(EntityManager entityManager, TransactionTemplate transaction) {
        this.entityManager = entityManager;
        this.transaction = transaction;
    }

    @Before
    public void emptyTables() {
        transaction.executeWithoutResult(status -> entityManager.createQuery("delete from Wallet").executeUpdate());
    }
}
