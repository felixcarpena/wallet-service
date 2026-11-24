package com.playtomic.tests.wallet.steps;

import io.cucumber.java.Before;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;

public class DatabaseCleaner {

    private final JdbcTemplate jdbc;

    public DatabaseCleaner(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Before
    public void emptyTables() {
        JdbcTestUtils.deleteFromTables(jdbc, "wallet_movement", "top_up", "wallet");
    }
}
