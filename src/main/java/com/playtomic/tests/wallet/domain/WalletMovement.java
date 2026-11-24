package com.playtomic.tests.wallet.domain;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

@Entity
@Table(name = "wallet_movement", uniqueConstraints = @UniqueConstraint(columnNames = {"type", "cause_id"}))
public class WalletMovement {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private WalletMovementType type;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "amount", precision = 19, scale = 4, nullable = false))
    @AttributeOverride(name = "currency", column = @Column(name = "currency", length = 3, nullable = false))
    private Money amount;

    @Column(name = "cause_id", nullable = false)
    private UUID causeId;

    protected WalletMovement() {
    }

    WalletMovement(Wallet wallet, WalletMovementType type, Money amount, UUID causeId) {
        this.id = UuidCreator.getTimeOrderedEpoch();
        this.wallet = wallet;
        this.type = type;
        this.amount = amount;
        this.causeId = causeId;
    }

    public WalletMovementType type() {
        return type;
    }

    public Money amount() {
        return amount;
    }

    public UUID causeId() {
        return causeId;
    }
}
