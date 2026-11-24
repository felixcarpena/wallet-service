package com.playtomic.tests.wallet.infrastructure.http;

import com.playtomic.tests.wallet.application.WalletReader;
import com.playtomic.tests.wallet.domain.WalletId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class WalletController {

    private final WalletReader walletReader;

    public WalletController(WalletReader walletReader) {
        this.walletReader = walletReader;
    }

    @GetMapping("/wallets/{id}")
    public WalletResponse findWallet(@PathVariable UUID id) {
        return WalletResponse.from(walletReader.findOrFail(new WalletId(id)));
    }
}
