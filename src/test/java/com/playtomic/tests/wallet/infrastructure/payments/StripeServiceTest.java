package com.playtomic.tests.wallet.infrastructure.payments;

import com.playtomic.tests.wallet.infrastructure.payments.exceptions.StripeAmountTooSmallException;
import com.playtomic.tests.wallet.infrastructure.payments.exceptions.StripeRequestRejectedException;
import com.playtomic.tests.wallet.infrastructure.payments.exceptions.StripeServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.client.MockServerRestTemplateCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class StripeServiceTest {

    private static final URI CHARGES_URI = URI.create("http://stripe.test/charges");
    private static final URI REFUNDS_URI = URI.create("http://stripe.test/refunds");
    private static final String CARD = "4242 4242 4242 4242";

    private final MockServerRestTemplateCustomizer customizer = new MockServerRestTemplateCustomizer();
    private final StripeService stripeService = new StripeService(CHARGES_URI, REFUNDS_URI,
            Duration.ofSeconds(1), Duration.ofSeconds(1), new RestTemplateBuilder(customizer));
    private final MockRestServiceServer server = customizer.getServer();

    @Test
    public void charge_throws_amount_too_small_when_provider_answers_422() {
        server.expect(requestTo(CHARGES_URI))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.amount").value(5))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY));

        assertThatThrownBy(() -> stripeService.charge(CARD, new BigDecimal(5)))
                .isInstanceOf(StripeAmountTooSmallException.class);
        server.verify();
    }

    @Test
    public void charge_throws_request_rejected_when_provider_answers_another_client_error() {
        server.expect(requestTo(CHARGES_URI)).andRespond(withStatus(HttpStatus.BAD_REQUEST));

        assertThatThrownBy(() -> stripeService.charge(CARD, new BigDecimal(15)))
                .isInstanceOf(StripeRequestRejectedException.class);
        server.verify();
    }

    @Test
    public void charge_throws_a_service_error_when_provider_answers_with_a_server_error() {
        server.expect(requestTo(CHARGES_URI)).andRespond(withServerError());

        assertThatThrownBy(() -> stripeService.charge(CARD, new BigDecimal(15)))
                .isExactlyInstanceOf(StripeServiceException.class);
        server.verify();
    }

    @Test
    public void charge_posts_card_and_amount_and_returns_the_payment() {
        server.expect(requestTo(CHARGES_URI))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.credit_card").value(CARD))
                .andExpect(jsonPath("$.amount").value(15))
                .andRespond(withSuccess("""
                        { "id": "payment-1" }
                        """, MediaType.APPLICATION_JSON));

        Payment payment = stripeService.charge(CARD, new BigDecimal(15));

        assertThat(payment).hasFieldOrPropertyWithValue("id", "payment-1");
        server.verify();
    }
}
