package com.playtomic.tests.wallet.domain.exceptions;

import com.playtomic.tests.wallet.domain.TopUpId;

public class TopUpNotFoundException extends RuntimeException {

    public TopUpNotFoundException(TopUpId id) {
        super("Top-up " + id.value() + " does not exist");
    }
}
