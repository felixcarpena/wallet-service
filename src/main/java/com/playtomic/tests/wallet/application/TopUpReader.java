package com.playtomic.tests.wallet.application;

import com.playtomic.tests.wallet.domain.TopUpId;
import com.playtomic.tests.wallet.domain.exceptions.TopUpNotFoundException;

public interface TopUpReader {

    TopUpView findOrFail(TopUpId id) throws TopUpNotFoundException;
}
