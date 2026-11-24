package com.playtomic.tests.wallet.application;

import com.playtomic.tests.wallet.domain.Money;
import com.playtomic.tests.wallet.domain.WalletId;

public record WalletView(WalletId id, Money balance) {
}
