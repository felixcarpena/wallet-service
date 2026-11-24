package com.playtomic.tests.wallet.infrastructure.persistence;

import com.playtomic.tests.wallet.application.WalletReader;
import com.playtomic.tests.wallet.application.WalletView;
import com.playtomic.tests.wallet.domain.Money;
import com.playtomic.tests.wallet.domain.Wallet;
import com.playtomic.tests.wallet.domain.WalletId;
import com.playtomic.tests.wallet.domain.Wallet_;
import com.playtomic.tests.wallet.domain.exceptions.WalletNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

@Repository
public class JpaWalletReader implements WalletReader {

    private final EntityManager entityManager;

    public JpaWalletReader(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public WalletView findOrFail(WalletId id) throws WalletNotFoundException {
        CriteriaBuilder criteria = entityManager.getCriteriaBuilder();
        CriteriaQuery<Money> query = criteria.createQuery(Money.class);
        Root<Wallet> wallet = query.from(Wallet.class);
        query.select(wallet.get(Wallet_.balance)).where(criteria.equal(wallet.get(Wallet_.id), id.value()));

        return entityManager.createQuery(query)
                .getResultList()
                .stream()
                .findFirst()
                .map(balance -> new WalletView(id, balance))
                .orElseThrow(() -> new WalletNotFoundException(id));
    }
}
