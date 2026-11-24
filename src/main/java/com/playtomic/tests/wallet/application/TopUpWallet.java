package com.playtomic.tests.wallet.application;

import com.playtomic.tests.wallet.domain.CardNumber;
import com.playtomic.tests.wallet.domain.ChargeResult;
import com.playtomic.tests.wallet.domain.CreditWallet;
import com.playtomic.tests.wallet.domain.Money;
import com.playtomic.tests.wallet.domain.PaymentProvider;
import com.playtomic.tests.wallet.domain.TopUp;
import com.playtomic.tests.wallet.domain.TopUpId;
import com.playtomic.tests.wallet.domain.TopUpRepository;
import com.playtomic.tests.wallet.domain.Wallet;
import com.playtomic.tests.wallet.domain.WalletId;
import com.playtomic.tests.wallet.domain.WalletRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

@Component
public class TopUpWallet {

    private final TopUpRepository topUps;
    private final WalletRepository wallets;
    private final PaymentProvider paymentProvider;
    private final TransactionTemplate transactions;

    public TopUpWallet(TopUpRepository topUps, WalletRepository wallets, PaymentProvider paymentProvider,
                       TransactionTemplate transactions) {
        this.topUps = topUps;
        this.wallets = wallets;
        this.paymentProvider = paymentProvider;
        this.transactions = transactions;
    }

    @Transactional(propagation = Propagation.NEVER)
    public void topUp(TopUpId topUpId, WalletId walletId, Money amount, CardNumber card) {
        start(topUpId, walletId, amount, card);
        ChargeResult result = paymentProvider.charge(card, amount);
        recordOutcome(topUpId, result).ifPresent(creditWallet -> credit(topUpId, creditWallet));
    }

    private void start(TopUpId topUpId, WalletId walletId, Money amount, CardNumber card) {
        transactions.executeWithoutResult(status -> {
            wallets.findOrFail(walletId).validateCredit(amount);
            topUps.save(TopUp.start(topUpId, walletId, amount, card));
        });
    }

    private Optional<CreditWallet> recordOutcome(TopUpId topUpId, ChargeResult result) {
        return transactions.execute(status -> {
            TopUp topUp = topUps.findForUpdateOrFail(topUpId);
            Optional<CreditWallet> creditWallet = topUp.on(result);
            topUps.save(topUp);
            return creditWallet;
        });
    }

    private void credit(TopUpId topUpId, CreditWallet creditWallet) {
        transactions.executeWithoutResult(status -> {
            TopUp topUp = topUps.findForUpdateOrFail(topUpId);
            Wallet wallet = wallets.findForUpdateOrFail(creditWallet.walletId());
            wallet.credit(creditWallet.amount(), creditWallet.topUpId());
            topUp.walletCredited();
            topUps.save(topUp);
            wallets.save(wallet);
        });
    }
}
