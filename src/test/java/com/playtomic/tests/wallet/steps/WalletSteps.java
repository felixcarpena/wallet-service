package com.playtomic.tests.wallet.steps;

import com.playtomic.tests.wallet.application.WalletReader;
import com.playtomic.tests.wallet.domain.Money;
import com.playtomic.tests.wallet.domain.Wallet;
import com.playtomic.tests.wallet.domain.WalletId;
import com.playtomic.tests.wallet.domain.WalletRepository;
import com.playtomic.tests.wallet.domain.exceptions.WalletNotFoundException;
import io.cucumber.java.en.Given;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class WalletSteps {

    private final WalletRepository wallets;
    private final WalletReader walletReader;
    private final TransactionTemplate transaction;

    public WalletSteps(WalletRepository wallets, WalletReader walletReader, TransactionTemplate transaction) {
        this.wallets = wallets;
        this.walletReader = walletReader;
        this.transaction = transaction;
    }

    @Given("a wallet {uuid} with a balance of {money}")
    public void aWalletWithABalanceOf(UUID id, Money balance) {
        transaction.executeWithoutResult(status -> wallets.save(new Wallet(new WalletId(id), balance)));
    }

    @Given("no wallet {uuid} exists")
    public void noWalletExists(UUID id) {
        assertThatThrownBy(() -> walletReader.findOrFail(new WalletId(id))).isInstanceOf(WalletNotFoundException.class);
    }
}
