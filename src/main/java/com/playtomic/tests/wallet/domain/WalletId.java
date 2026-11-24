package com.playtomic.tests.wallet.domain;

import java.util.Objects;
import java.util.UUID;

public record WalletId(UUID value) {

    public WalletId {
        Objects.requireNonNull(value, "value");
    }
}
