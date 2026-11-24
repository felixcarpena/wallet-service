package com.playtomic.tests.wallet.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.playtomic.tests.wallet.JsonText;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class WalletLookupSteps {

    private final TestRestTemplate http;
    private final ScenarioResponse response;

    public WalletLookupSteps(TestRestTemplate http, ScenarioResponse response) {
        this.http = http;
        this.response = response;
    }

    @When("the player requests the wallet {uuid}")
    public void thePlayerRequestsTheWallet(UUID id) {
        response.remember(http.getForEntity("/wallets/{id}", String.class, id));
    }

    @Then("the wallet is returned with:")
    public void theWalletIsReturnedWith(DataTable expected) throws Exception {
        Map<String, String> row = expected.asMaps().get(0);
        String[] expectedBalance = row.get("balance").split(" ");
        assertThat(response.last().getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode body = JsonText.read(response.last().getBody());
        assertThat(body.get("id").asText()).isEqualTo(row.get("id"));
        assertThat(body.get("balance").get("amount").decimalValue()).isEqualTo(new BigDecimal(expectedBalance[0]));
        assertThat(body.get("balance").get("currency").asText()).isEqualTo(expectedBalance[1]);
    }

    @Then("the wallet is reported as not found")
    public void theWalletIsReportedAsNotFound() {
        assertThat(response.last().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.last().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
    }
}
