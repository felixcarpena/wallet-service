package com.playtomic.tests.wallet.infrastructure.http;

import com.playtomic.tests.wallet.application.WalletView;

import java.util.UUID;

public record WalletResponse(UUID id, MoneyResponse balance) {

    static WalletResponse from(WalletView wallet) {
        return new WalletResponse(wallet.id().value(), MoneyResponse.from(wallet.balance()));
    }
}
