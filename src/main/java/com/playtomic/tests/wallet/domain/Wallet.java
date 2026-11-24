package com.playtomic.tests.wallet.domain;

import com.playtomic.tests.wallet.domain.exceptions.NonPositiveAmountException;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.PERSIST)
    private List<WalletMovement> movements = new ArrayList<>();

    protected Wallet() {
    }

    public Wallet(WalletId id, Money balance) {
        this.id = Objects.requireNonNull(id, "id").value();
        this.balance = Objects.requireNonNull(balance, "balance");
    }

    public Money balance() {
        return balance;
    }

    public List<WalletMovement> movements() {
        return Collections.unmodifiableList(movements);
    }

    public WalletMovement credit(Money amount, TopUpId topUpId) {
        if (!amount.isPositive()) {
            throw new NonPositiveAmountException(amount);
        }
        Money newBalance = balance.add(amount);
        WalletMovement movement = new WalletMovement(this, WalletMovementType.TOP_UP, amount, topUpId.value());
        movements.add(movement);
        balance = newBalance;
        return movement;
    }
}
