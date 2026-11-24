package com.playtomic.tests.wallet.infrastructure.http;

import com.playtomic.tests.wallet.domain.Money;
import com.playtomic.tests.wallet.infrastructure.http.exceptions.InvalidTopUpRequestException;

public record TopUpRequest(MoneyRequest amount, String cardNumber) {

    Money money() {
        if (amount == null) {
            throw new InvalidTopUpRequestException("A top-up needs an amount");
        }
        return amount.toMoney();
    }
}
