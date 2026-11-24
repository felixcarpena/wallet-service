package com.playtomic.tests.wallet.steps;

import io.cucumber.spring.ScenarioScope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@ScenarioScope
public class ScenarioResponse {

    private ResponseEntity<String> last;

    public void remember(ResponseEntity<String> response) {
        this.last = response;
    }

    public ResponseEntity<String> last() {
        return last;
    }
}
