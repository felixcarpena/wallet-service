package com.playtomic.tests.wallet.domain.exceptions;

import com.playtomic.tests.wallet.domain.TopUpId;
import com.playtomic.tests.wallet.domain.TopUpStatus;

public class IllegalTopUpTransitionException extends RuntimeException {

    public IllegalTopUpTransitionException(TopUpId id, TopUpStatus expected, TopUpStatus actual) {
        super("Top-up " + id.value() + " must be " + expected + " for this step but is " + actual);
    }
}
