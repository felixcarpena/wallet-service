package com.playtomic.tests.wallet.domain;

import com.playtomic.tests.wallet.domain.exceptions.WalletNotFoundException;

public interface WalletRepository {

    void save(Wallet wallet);

    Wallet findForUpdateOrFail(WalletId id) throws WalletNotFoundException;
}
