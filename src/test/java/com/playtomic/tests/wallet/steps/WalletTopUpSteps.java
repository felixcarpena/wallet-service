package com.playtomic.tests.wallet.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.playtomic.tests.wallet.JsonText;
import com.playtomic.tests.wallet.domain.CardNumber;
import com.playtomic.tests.wallet.domain.Money;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class WalletTopUpSteps {

    private final TestRestTemplate http;
    private final ScenarioResponse response;

    public WalletTopUpSteps(TestRestTemplate http, ScenarioResponse response) {
        this.http = http;
        this.response = response;
    }

    @When("the player tops up {amount} on wallet {uuid} with card {card}")
    public void thePlayerTopsUp(RequestedAmount amount, UUID wallet, CardNumber card) {
        topUp(wallet, """
                { "amount": { "amount": %s, "currency": "%s" }, "cardNumber": "%s" }
                """.formatted(amount.value(), amount.currency(), card.value()));
    }

    @When("the player tops up {amount} on wallet {uuid} without a card number")
    public void thePlayerTopsUpWithoutACardNumber(RequestedAmount amount, UUID wallet) {
        topUp(wallet, """
                { "amount": { "amount": %s, "currency": "%s" } }
                """.formatted(amount.value(), amount.currency()));
    }

    @When("the player tops up wallet {uuid} with card {card} without an amount")
    public void thePlayerTopsUpWithoutAnAmount(UUID wallet, CardNumber card) {
        topUp(wallet, """
                { "cardNumber": "%s" }
                """.formatted(card.value()));
    }

    @When("the player tops up {bigdecimal} without a currency on wallet {uuid} with card {card}")
    public void thePlayerTopsUpWithoutACurrency(BigDecimal value, UUID wallet, CardNumber card) {
        topUp(wallet, """
                { "amount": { "amount": %s }, "cardNumber": "%s" }
                """.formatted(value.toPlainString(), card.value()));
    }

    @Then("the top-up is reported as completed")
    public void theTopUpIsReportedAsCompleted() throws Exception {
        assertThat(response.last().getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(JsonText.read(response.last().getBody()).get("status").asText()).isEqualTo("COMPLETED");
    }

    @Then("the top-up is reported as invalid")
    public void theTopUpIsReportedAsInvalid() {
        assertThat(response.last().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.last().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
    }

    private void topUp(UUID wallet, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        response.remember(http.postForEntity("/wallets/{id}/top-ups", new HttpEntity<>(body, headers), String.class, wallet));
    }

    @Then("the balance of wallet {uuid} is {money}")
    public void theBalanceOfWalletIs(UUID wallet, Money balance) throws Exception {
        JsonNode body = JsonText.read(http.getForEntity("/wallets/{id}", String.class, wallet).getBody());
        Money returned = new Money(new BigDecimal(body.get("balance").get("amount").asText()),
                Currency.getInstance(body.get("balance").get("currency").asText()));
        assertThat(returned).isEqualTo(balance);
    }
}
