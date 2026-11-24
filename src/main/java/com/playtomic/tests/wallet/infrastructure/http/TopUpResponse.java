package com.playtomic.tests.wallet.infrastructure.http;

import com.playtomic.tests.wallet.application.TopUpView;

import java.util.UUID;

public record TopUpResponse(UUID id, String status, MoneyResponse amount) {

    static TopUpResponse from(TopUpView topUp) {
        return new TopUpResponse(topUp.id().value(), topUp.status().name(), MoneyResponse.from(topUp.amount()));
    }
}
