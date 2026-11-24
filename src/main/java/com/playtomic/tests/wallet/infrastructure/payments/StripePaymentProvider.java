package com.playtomic.tests.wallet.infrastructure.payments;

import com.playtomic.tests.wallet.domain.CardNumber;
import com.playtomic.tests.wallet.domain.ChargeResult;
import com.playtomic.tests.wallet.domain.Money;
import com.playtomic.tests.wallet.domain.PaymentProvider;
import com.playtomic.tests.wallet.domain.PaymentReference;
import com.playtomic.tests.wallet.infrastructure.payments.exceptions.StripeAmountTooSmallException;
import com.playtomic.tests.wallet.infrastructure.payments.exceptions.StripeRequestRejectedException;
import com.playtomic.tests.wallet.infrastructure.payments.exceptions.StripeServiceException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.ConnectException;

@Component
public class StripePaymentProvider implements PaymentProvider {

    private final StripeService stripeService;

    public StripePaymentProvider(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @Override
    public ChargeResult charge(CardNumber card, Money amount) {
        try {
            Payment payment = stripeService.charge(card.value(), inCurrencyDecimals(amount));
            if (payment == null || payment.getId() == null) {
                return new ChargeResult.Unconfirmed();
            }
            return new ChargeResult.Charged(new PaymentReference(payment.getId()));
        } catch (StripeAmountTooSmallException declined) {
            return new ChargeResult.Declined();
        } catch (StripeRequestRejectedException rejected) {
            return new ChargeResult.Failed();
        } catch (ResourceAccessException noAnswer) {
            return noAnswer.getCause() instanceof ConnectException ? new ChargeResult.Failed() : new ChargeResult.Unconfirmed();
        } catch (StripeServiceException | RestClientException unexpectedAnswer) {
            return new ChargeResult.Unconfirmed();
        }
    }

    private static BigDecimal inCurrencyDecimals(Money amount) {
        return amount.amount().setScale(amount.currency().getDefaultFractionDigits(), RoundingMode.UNNECESSARY);
    }
}
