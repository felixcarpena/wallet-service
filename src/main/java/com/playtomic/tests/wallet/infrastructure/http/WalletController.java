package com.playtomic.tests.wallet.infrastructure.http;

import com.playtomic.tests.wallet.application.TopUpReader;
import com.playtomic.tests.wallet.application.TopUpView;
import com.playtomic.tests.wallet.application.TopUpWallet;
import com.playtomic.tests.wallet.application.WalletReader;
import com.playtomic.tests.wallet.domain.CardNumber;
import com.playtomic.tests.wallet.domain.TopUpId;
import com.playtomic.tests.wallet.domain.TopUpStatus;
import com.playtomic.tests.wallet.domain.WalletId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class WalletController {

    private final WalletReader walletReader;
    private final TopUpWallet topUpWallet;
    private final TopUpReader topUpReader;

    public WalletController(WalletReader walletReader, TopUpWallet topUpWallet, TopUpReader topUpReader) {
        this.walletReader = walletReader;
        this.topUpWallet = topUpWallet;
        this.topUpReader = topUpReader;
    }

    @GetMapping("/wallets/{id}")
    public WalletResponse findWallet(@PathVariable UUID id) {
        return WalletResponse.from(walletReader.findOrFail(new WalletId(id)));
    }

    @PostMapping("/wallets/{id}/top-ups")
    public ResponseEntity<TopUpResponse> topUp(@PathVariable UUID id, @RequestBody TopUpRequest request) {
        TopUpId topUpId = TopUpId.generate();
        topUpWallet.topUp(topUpId, new WalletId(id), request.money(), new CardNumber(request.cardNumber()));
        TopUpView topUp = topUpReader.findOrFail(topUpId);
        return ResponseEntity.status(httpStatusOf(topUp.status())).body(TopUpResponse.from(topUp));
    }

    private static HttpStatus httpStatusOf(TopUpStatus status) {
        return switch (status) {
            case COMPLETED -> HttpStatus.CREATED;
            case DECLINED -> HttpStatus.UNPROCESSABLE_ENTITY;
            case FAILED -> HttpStatus.BAD_GATEWAY;
            case UNCONFIRMED -> HttpStatus.ACCEPTED;
            case PENDING, CHARGED -> throw new IllegalStateException("A top-up cannot be answered while " + status);
        };
    }
}
