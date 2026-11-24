package com.playtomic.tests.wallet.steps;

import com.playtomic.tests.wallet.domain.Money;
import com.playtomic.tests.wallet.domain.Wallet;
import com.playtomic.tests.wallet.domain.WalletId;
import com.playtomic.tests.wallet.domain.WalletRepository;
import io.cucumber.java.en.Given;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

public class WalletSteps {

    private final WalletRepository wallets;
    private final TransactionTemplate transaction;

    public WalletSteps(WalletRepository wallets, TransactionTemplate transaction) {
        this.wallets = wallets;
        this.transaction = transaction;
    }

    @Given("a wallet {uuid} with a balance of {money}")
    public void aWalletWithABalanceOf(UUID id, Money balance) {
        transaction.executeWithoutResult(status -> wallets.save(new Wallet(new WalletId(id), balance)));
    }
}
