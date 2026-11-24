package com.playtomic.tests.wallet.domain;

import com.playtomic.tests.wallet.domain.exceptions.TopUpNotFoundException;

public interface TopUpRepository {

    void save(TopUp topUp);

    TopUp findForUpdateOrFail(TopUpId id) throws TopUpNotFoundException;
}
