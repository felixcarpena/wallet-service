package com.playtomic.tests.wallet.domain;

public interface PaymentProvider {

    ChargeResult charge(CardNumber card, Money amount);
}
