package com.playtomic.tests.wallet.infrastructure.persistence;

import com.playtomic.tests.wallet.application.TopUpReader;
import com.playtomic.tests.wallet.application.TopUpView;
import com.playtomic.tests.wallet.domain.Money;
import com.playtomic.tests.wallet.domain.TopUp;
import com.playtomic.tests.wallet.domain.TopUpId;
import com.playtomic.tests.wallet.domain.TopUpStatus;
import com.playtomic.tests.wallet.domain.TopUp_;
import com.playtomic.tests.wallet.domain.exceptions.TopUpNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

@Repository
public class JpaTopUpReader implements TopUpReader {

    private final EntityManager entityManager;

    public JpaTopUpReader(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public TopUpView findOrFail(TopUpId id) throws TopUpNotFoundException {
        CriteriaBuilder criteria = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = criteria.createTupleQuery();
        Root<TopUp> topUp = query.from(TopUp.class);
        query.multiselect(topUp.get(TopUp_.status), topUp.get(TopUp_.amount))
                .where(criteria.equal(topUp.get(TopUp_.id), id.value()));

        return entityManager.createQuery(query)
                .getResultList()
                .stream()
                .findFirst()
                .map(row -> new TopUpView(id, row.get(0, TopUpStatus.class), row.get(1, Money.class)))
                .orElseThrow(() -> new TopUpNotFoundException(id));
    }
}
