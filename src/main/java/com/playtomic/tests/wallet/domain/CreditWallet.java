package com.playtomic.tests.wallet.domain;

public record CreditWallet(WalletId walletId, Money amount, TopUpId topUpId) {
}
