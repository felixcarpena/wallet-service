package com.playtomic.tests.wallet.domain;

import com.playtomic.tests.wallet.domain.exceptions.IllegalTopUpTransitionException;
import com.playtomic.tests.wallet.domain.exceptions.TooManyDecimalsException;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Optional;
import java.util.UUID;

@Entity
@Table(name = "top_up")
public class TopUp {

    @Id
    private UUID id;

    @Column(name = "wallet_id", nullable = false)
    private UUID walletId;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "amount", precision = 19, scale = 4, nullable = false))
    @AttributeOverride(name = "currency", column = @Column(name = "currency", length = 3, nullable = false))
    private Money amount;

    @Column(name = "masked_card_number", nullable = false, length = 9)
    private String maskedCardNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TopUpStatus status;

    @Column(name = "payment_reference")
    private String paymentReference;

    protected TopUp() {
    }

    private TopUp(TopUpId id, WalletId walletId, Money amount, String maskedCardNumber) {
        this.id = id.value();
        this.walletId = walletId.value();
        this.amount = amount;
        this.maskedCardNumber = maskedCardNumber;
        this.status = TopUpStatus.PENDING;
    }

    public static TopUp start(TopUpId id, WalletId walletId, Money amount, CardNumber card) {
        if (amount.amount().stripTrailingZeros().scale() > amount.currency().getDefaultFractionDigits()) {
            throw new TooManyDecimalsException(amount.amount(), amount.currency().getDefaultFractionDigits());
        }
        return new TopUp(id, walletId, amount, card.masked());
    }

    public Optional<CreditWallet> on(ChargeResult result) {
        requireStatus(TopUpStatus.PENDING);
        if (result instanceof ChargeResult.Charged charged) {
            this.paymentReference = charged.reference().value();
            this.status = TopUpStatus.CHARGED;
            return Optional.of(new CreditWallet(walletId(), amount, id()));
        }
        this.status = statusAfter(result);
        return Optional.empty();
    }

    private static TopUpStatus statusAfter(ChargeResult result) {
        if (result instanceof ChargeResult.Declined) {
            return TopUpStatus.DECLINED;
        }
        if (result instanceof ChargeResult.Failed) {
            return TopUpStatus.FAILED;
        }
        if (result instanceof ChargeResult.Unconfirmed) {
            return TopUpStatus.UNCONFIRMED;
        }
        throw new IllegalStateException("Unhandled charge result " + result);
    }

    public void walletCredited() {
        requireStatus(TopUpStatus.CHARGED);
        this.status = TopUpStatus.COMPLETED;
    }

    private void requireStatus(TopUpStatus expected) {
        if (status != expected) {
            throw new IllegalTopUpTransitionException(id(), expected, status);
        }
    }

    public TopUpId id() {
        return new TopUpId(id);
    }

    private WalletId walletId() {
        return new WalletId(walletId);
    }

    public TopUpStatus status() {
        return status;
    }

    public Optional<PaymentReference> paymentReference() {
        return Optional.ofNullable(paymentReference).map(PaymentReference::new);
    }
}
