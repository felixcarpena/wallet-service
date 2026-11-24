package com.playtomic.tests.wallet.domain.exceptions;

import com.playtomic.tests.wallet.domain.WalletId;

public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException(WalletId id) {
        super("Wallet " + id.value() + " does not exist");
    }
}
