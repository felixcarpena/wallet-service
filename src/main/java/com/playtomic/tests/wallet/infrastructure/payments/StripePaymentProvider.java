package com.playtomic.tests.wallet.infrastructure.payments;

import com.playtomic.tests.wallet.domain.CardNumber;
import com.playtomic.tests.wallet.domain.ChargeResult;
import com.playtomic.tests.wallet.domain.Money;
import com.playtomic.tests.wallet.domain.PaymentProvider;
import com.playtomic.tests.wallet.domain.PaymentReference;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;

@Component
public class StripePaymentProvider implements PaymentProvider {

    private final StripeService stripeService;

    public StripePaymentProvider(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @Override
    public ChargeResult charge(CardNumber card, Money amount) {
        int decimals = amount.currency().getDefaultFractionDigits();
        Payment payment = stripeService.charge(card.value(), amount.amount().setScale(decimals, RoundingMode.UNNECESSARY));
        return new ChargeResult.Charged(new PaymentReference(payment.getId()));
    }
}
