package com.playtomic.tests.wallet.application;

import com.playtomic.tests.wallet.domain.WalletId;
import com.playtomic.tests.wallet.domain.exceptions.WalletNotFoundException;

public interface WalletReader {

    WalletView findOrFail(WalletId id) throws WalletNotFoundException;
}
