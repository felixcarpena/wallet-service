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
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class WalletTopUpSteps {

    private final TestRestTemplate http;
    private ResponseEntity<String> response;

    public WalletTopUpSteps(TestRestTemplate http) {
        this.http = http;
    }

    @When("the player tops up {money} on wallet {uuid} with card {card}")
    public void thePlayerTopsUp(Money amount, UUID wallet, CardNumber card) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = """
                { "amount": { "amount": %s, "currency": "%s" }, "cardNumber": "%s" }
                """.formatted(amount.amount().toPlainString(), amount.currency().getCurrencyCode(), card.value());
        response = http.postForEntity("/wallets/{id}/top-ups", new HttpEntity<>(body, headers), String.class, wallet);
    }

    @Then("the top-up is reported as completed")
    public void theTopUpIsReportedAsCompleted() throws Exception {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(JsonText.read(response.getBody()).get("status").asText()).isEqualTo("COMPLETED");
    }

    @Then("the balance of wallet {uuid} is {money}")
    public void theBalanceOfWalletIs(UUID wallet, Money balance) throws Exception {
        JsonNode body = JsonText.read(http.getForEntity("/wallets/{id}", String.class, wallet).getBody());
        Money returned = new Money(new BigDecimal(body.get("balance").get("amount").asText()),
                Currency.getInstance(body.get("balance").get("currency").asText()));
        assertThat(returned).isEqualTo(balance);
    }
}
