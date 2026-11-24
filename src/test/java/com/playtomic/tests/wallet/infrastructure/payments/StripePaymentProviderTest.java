package com.playtomic.tests.wallet.infrastructure.payments;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playtomic.tests.wallet.domain.CardNumber;
import com.playtomic.tests.wallet.domain.ChargeResult;
import com.playtomic.tests.wallet.domain.Money;
import com.playtomic.tests.wallet.domain.PaymentReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Currency;

import static com.playtomic.tests.wallet.JsonText.compact;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class StripePaymentProviderTest {

    private static final URI CHARGES_URI = URI.create("http://stripe.test/charges");
    private static final URI REFUNDS_URI = URI.create("http://stripe.test/refunds");
    private static final CardNumber CARD = new CardNumber("4242 4242 4242 4242");

    private final MockServerRestTemplateCustomizer customizer = new MockServerRestTemplateCustomizer();
    private final StripePaymentProvider paymentProvider = new StripePaymentProvider(
            new StripeService(CHARGES_URI, REFUNDS_URI, new RestTemplateBuilder(customizer)));
    private final MockRestServiceServer server = customizer.getServer();

    @Test
    void a_successful_charge_returns_the_payment_reference() throws Exception {
        server.expect(requestTo(CHARGES_URI))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(compact("""
                        {
                          "credit_card": "4242 4242 4242 4242",
                          "amount": 15.00
                        }
                        """)))
                .andRespond(withSuccess("""
                        { "id": "payment-1" }
                        """, MediaType.APPLICATION_JSON));

        ChargeResult result = paymentProvider.charge(CARD, new Money(new BigDecimal("15"), Currency.getInstance("EUR")));

        assertThat(result).isEqualTo(new ChargeResult.Charged(new PaymentReference("payment-1")));
        server.verify();
    }
}
