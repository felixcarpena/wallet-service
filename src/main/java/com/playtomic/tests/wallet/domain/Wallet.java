package com.playtomic.tests.wallet.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "wallet")
public class Wallet {

    @Id
    private UUID id;

    @Embedded
    @AttributeOverride(name = "amount", column = @Column(name = "balance_amount", precision = 19, scale = 4, nullable = false))
    @AttributeOverride(name = "currency", column = @Column(name = "balance_currency", length = 3, nullable = false))
    private Money balance;

    protected Wallet() {
    }

    public Wallet(WalletId id, Money balance) {
        this.id = Objects.requireNonNull(id, "id").value();
        this.balance = Objects.requireNonNull(balance, "balance");
    }
}
