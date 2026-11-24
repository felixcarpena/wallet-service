package com.playtomic.tests.wallet.infrastructure.payments;

import com.playtomic.tests.wallet.domain.CardNumber;
import com.playtomic.tests.wallet.domain.ChargeResult;
import com.playtomic.tests.wallet.domain.Money;
import com.playtomic.tests.wallet.domain.PaymentReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.ResponseCreator;

import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.time.Duration;
import java.util.Currency;

import static com.playtomic.tests.wallet.JsonText.compact;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class StripePaymentProviderTest {

    private static final URI CHARGES_URI = URI.create("http://stripe.test/charges");
    private static final URI REFUNDS_URI = URI.create("http://stripe.test/refunds");
    private static final CardNumber CARD = new CardNumber("4242 4242 4242 4242");
    private static final Money AMOUNT = new Money(new BigDecimal("15"), Currency.getInstance("EUR"));

    private final MockServerRestTemplateCustomizer customizer = new MockServerRestTemplateCustomizer();
    private final StripePaymentProvider paymentProvider = new StripePaymentProvider(new StripeService(CHARGES_URI, REFUNDS_URI,
            Duration.ofSeconds(1), Duration.ofSeconds(1), new RestTemplateBuilder(customizer)));
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

        ChargeResult result = paymentProvider.charge(CARD, AMOUNT);

        assertThat(result).isEqualTo(new ChargeResult.Charged(new PaymentReference("payment-1")));
        server.verify();
    }

    @Test
    void a_rejected_charge_is_declined() {
        assertThat(chargeAnswered(withStatus(HttpStatus.UNPROCESSABLE_ENTITY))).isEqualTo(new ChargeResult.Declined());
    }

    @Test
    void a_provider_that_cannot_be_reached_fails_the_charge() {
        assertThat(chargeAnswered(withException(new ConnectException("Connection refused")))).isEqualTo(new ChargeResult.Failed());
    }

    @Test
    void a_server_error_leaves_the_charge_unconfirmed() {
        assertThat(chargeAnswered(withServerError())).isEqualTo(new ChargeResult.Unconfirmed());
    }

    @Test
    void an_answer_without_a_payment_reference_leaves_the_charge_unconfirmed() {
        assertThat(chargeAnswered(withSuccess("{}", MediaType.APPLICATION_JSON))).isEqualTo(new ChargeResult.Unconfirmed());
    }

    @Test
    void an_answer_with_an_empty_payment_reference_leaves_the_charge_unconfirmed() {
        assertThat(chargeAnswered(withSuccess("""
                { "id": null }
                """, MediaType.APPLICATION_JSON))).isEqualTo(new ChargeResult.Unconfirmed());
    }

    @Test
    void an_answer_without_a_body_leaves_the_charge_unconfirmed() {
        assertThat(chargeAnswered(withSuccess())).isEqualTo(new ChargeResult.Unconfirmed());
    }

    @Test
    void a_timed_out_answer_leaves_the_charge_unconfirmed() {
        assertThat(chargeAnswered(withException(new SocketTimeoutException("Read timed out")))).isEqualTo(new ChargeResult.Unconfirmed());
    }

    private ChargeResult chargeAnswered(ResponseCreator answer) {
        server.expect(requestTo(CHARGES_URI)).andRespond(answer);
        ChargeResult result = paymentProvider.charge(CARD, AMOUNT);
        server.verify();
        return result;
    }
}
