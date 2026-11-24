package com.playtomic.tests.wallet.infrastructure.http;

import com.playtomic.tests.wallet.application.WalletReader;
import com.playtomic.tests.wallet.application.WalletView;
import com.playtomic.tests.wallet.domain.Money;
import com.playtomic.tests.wallet.domain.WalletId;
import com.playtomic.tests.wallet.domain.exceptions.WalletNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static com.playtomic.tests.wallet.JsonText.compact;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WalletController.class)
class WalletControllerTest {

    private static final UUID ID = UUID.fromString("00000000-0000-7000-8000-000000000001");

    @Autowired
    private MockMvc mvc;

    @MockBean
    private WalletReader walletReader;

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
}
