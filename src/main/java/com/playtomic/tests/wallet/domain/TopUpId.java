package com.playtomic.tests.wallet.domain;

import com.github.f4b6a3.uuid.UuidCreator;

import java.util.Objects;
import java.util.UUID;

public record TopUpId(UUID value) {

    public TopUpId {
        Objects.requireNonNull(value, "value");
    }

    public static TopUpId generate() {
        return new TopUpId(UuidCreator.getTimeOrderedEpoch());
    }
}
