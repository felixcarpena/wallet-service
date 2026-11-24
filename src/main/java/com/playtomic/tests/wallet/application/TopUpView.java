package com.playtomic.tests.wallet.application;

import com.playtomic.tests.wallet.domain.Money;
import com.playtomic.tests.wallet.domain.TopUpId;
import com.playtomic.tests.wallet.domain.TopUpStatus;

public record TopUpView(TopUpId id, TopUpStatus status, Money amount) {
}
