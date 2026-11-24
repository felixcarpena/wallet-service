package com.playtomic.tests.wallet.steps;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.playtomic.tests.wallet.domain.CardNumber;
import com.playtomic.tests.wallet.domain.Money;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import java.math.RoundingMode;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class PaymentProviderSteps {

    private static final WireMockServer PROVIDER = new WireMockServer(options().port(9999));

    @BeforeAll
    public static void startProvider() {
        PROVIDER.start();
    }

    @AfterAll
    public static void stopProvider() {
        PROVIDER.stop();
    }

    @Before
    public void forgetPreviousScenario() {
        if (!PROVIDER.isRunning()) {
            PROVIDER.start();
        }
        PROVIDER.resetAll();
    }

    @Given("the payment provider accepts the charge")
    public void thePaymentProviderAcceptsTheCharge() {
        PROVIDER.stubFor(post(urlEqualTo("/")).willReturn(okJson("""
                { "id": "payment-1" }
                """)));
    }

    @Given("the payment provider accepts the charge without a payment reference")
    public void thePaymentProviderAcceptsTheChargeWithoutAPaymentReference() {
        PROVIDER.stubFor(post(urlEqualTo("/")).willReturn(okJson("{}")));
    }

    @Given("the payment provider declines the charge")
    public void thePaymentProviderDeclinesTheCharge() {
        PROVIDER.stubFor(post(urlEqualTo("/")).willReturn(aResponse().withStatus(422)));
    }

    @Given("the payment provider rejects the charge request")
    public void thePaymentProviderRejectsTheChargeRequest() {
        PROVIDER.stubFor(post(urlEqualTo("/")).willReturn(aResponse().withStatus(400)));
    }

    @Given("the payment provider answers with a server error")
    public void thePaymentProviderAnswersWithAServerError() {
        PROVIDER.stubFor(post(urlEqualTo("/")).willReturn(serverError()));
    }

    @Given("the payment provider does not answer in time")
    public void thePaymentProviderDoesNotAnswerInTime() {
        PROVIDER.stubFor(post(urlEqualTo("/")).willReturn(okJson("""
                { "id": "payment-1" }
                """).withFixedDelay(2_000)));
    }

    @Given("the payment provider cannot be reached")
    public void thePaymentProviderCannotBeReached() {
        PROVIDER.stop();
    }

    @Then("the payment provider was not asked to charge anything")
    public void thePaymentProviderWasNotAskedToChargeAnything() {
        PROVIDER.verify(0, postRequestedFor(urlEqualTo("/")));
    }

    @Then("the payment provider was asked to charge {money} on card {card}")
    public void thePaymentProviderWasAskedToCharge(Money amount, CardNumber card) {
        String charged = amount.amount().setScale(amount.currency().getDefaultFractionDigits(), RoundingMode.UNNECESSARY).toPlainString();
        PROVIDER.verify(1, postRequestedFor(urlEqualTo("/")).withRequestBody(equalToJson("""
                { "credit_card": "%s", "amount": %s }
                """.formatted(card.value(), charged))));
    }
}
