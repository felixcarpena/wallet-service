package com.playtomic.tests.wallet.infrastructure.http;

import com.playtomic.tests.wallet.application.TopUpReader;
import com.playtomic.tests.wallet.application.TopUpView;
import com.playtomic.tests.wallet.application.TopUpWallet;
import com.playtomic.tests.wallet.application.WalletReader;
import com.playtomic.tests.wallet.application.WalletView;
import com.playtomic.tests.wallet.domain.CardNumber;
import com.playtomic.tests.wallet.domain.Money;
import com.playtomic.tests.wallet.domain.TopUpId;
import com.playtomic.tests.wallet.domain.TopUpStatus;
import com.playtomic.tests.wallet.domain.WalletId;
import com.playtomic.tests.wallet.domain.exceptions.NonPositiveAmountException;
import com.playtomic.tests.wallet.domain.exceptions.WalletNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static com.playtomic.tests.wallet.JsonText.compact;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WalletController.class)
@ActiveProfiles("test")
class WalletControllerTest {

    private static final UUID ID = UUID.fromString("00000000-0000-7000-8000-000000000001");

    @Autowired
    private MockMvc mvc;

    @MockBean
    private WalletReader walletReader;

    @MockBean
    private TopUpWallet topUpWallet;

    @MockBean
    private TopUpReader topUpReader;

    @Test
    void returns_the_wallet_with_its_balance_in_the_currency_decimals() throws Exception {
        Money balance = new Money(new BigDecimal("25.4999"), Currency.getInstance("EUR"));
        when(walletReader.findOrFail(new WalletId(ID))).thenReturn(new WalletView(new WalletId(ID), balance));

        mvc.perform(get("/wallets/{id}", ID))
                .andExpect(status().isOk())
                .andExpect(content().string(compact("""
                        {
                          "id": "00000000-0000-7000-8000-000000000001",
                          "balance": {
                            "amount": 25.50,
                            "currency": "EUR"
                          }
                        }
                        """)));
    }

    @Test
    void reports_an_unknown_wallet_as_not_found() throws Exception {
        when(walletReader.findOrFail(new WalletId(ID))).thenThrow(new WalletNotFoundException(new WalletId(ID)));

        mvc.perform(get("/wallets/{id}", ID))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(content().json("""
                        {
                          "title": "Wallet not found",
                          "status": 404,
                          "detail": "Wallet 00000000-0000-7000-8000-000000000001 does not exist"
                        }
                        """));
    }

    @Test
    void a_top_up_is_created_and_answered_with_its_recorded_state() throws Exception {
        Money amount = new Money(new BigDecimal("15.00"), Currency.getInstance("EUR"));
        when(topUpReader.findOrFail(any())).thenAnswer(invocation ->
                new TopUpView(invocation.getArgument(0), TopUpStatus.COMPLETED, amount));

        String body = mvc.perform(post("/wallets/{id}/top-ups", ID).contentType(MediaType.APPLICATION_JSON).content("""
                        { "amount": { "amount": 15.00, "currency": "EUR" }, "cardNumber": "4242 4242 4242 4242" }
                        """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        ArgumentCaptor<TopUpId> topUpId = ArgumentCaptor.forClass(TopUpId.class);
        verify(topUpWallet).topUp(topUpId.capture(), eq(new WalletId(ID)), eq(amount), eq(new CardNumber("4242 4242 4242 4242")));
        assertThat(body).isEqualTo(compact("""
                {
                  "id": "%s",
                  "status": "COMPLETED",
                  "amount": { "amount": 15.00, "currency": "EUR" }
                }
                """.formatted(topUpId.getValue().value())));
    }

    @Test
    void an_invalid_top_up_is_reported_as_a_bad_request() throws Exception {
        doThrow(new NonPositiveAmountException(new Money(BigDecimal.ZERO, Currency.getInstance("EUR"))))
                .when(topUpWallet).topUp(any(), any(), any(), any());

        mvc.perform(post("/wallets/{id}/top-ups", ID).contentType(MediaType.APPLICATION_JSON).content("""
                        { "amount": { "amount": 0.00, "currency": "EUR" }, "cardNumber": "4242 4242 4242 4242" }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(content().json("""
                        { "title": "Invalid top-up", "status": 400 }
                        """));
    }

    @ParameterizedTest
    @CsvSource({"COMPLETED, 201", "DECLINED, 422", "FAILED, 502", "UNCONFIRMED, 202"})
    void a_top_up_is_answered_with_the_http_status_of_its_outcome(TopUpStatus outcome, int httpStatus) throws Exception {
        Money amount = new Money(new BigDecimal("15.00"), Currency.getInstance("EUR"));
        when(topUpReader.findOrFail(any())).thenAnswer(invocation -> new TopUpView(invocation.getArgument(0), outcome, amount));

        mvc.perform(post("/wallets/{id}/top-ups", ID).contentType(MediaType.APPLICATION_JSON).content("""
                        { "amount": { "amount": 15.00, "currency": "EUR" }, "cardNumber": "4242 4242 4242 4242" }
                        """))
                .andExpect(status().is(httpStatus))
                .andExpect(content().json("""
                        { "status": "%s" }
                        """.formatted(outcome)));
    }

    @Test
    void a_malformed_wallet_id_is_reported_as_a_bad_request() throws Exception {
        mvc.perform(get("/wallets/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"));
    }

    @Test
    void a_top_up_without_a_body_is_reported_as_a_bad_request() throws Exception {
        mvc.perform(post("/wallets/{id}/top-ups", ID).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"));
    }
}
