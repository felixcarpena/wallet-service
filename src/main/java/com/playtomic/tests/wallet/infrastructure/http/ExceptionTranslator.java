package com.playtomic.tests.wallet.infrastructure.http;

import com.playtomic.tests.wallet.domain.exceptions.WalletNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionTranslator {

    @ExceptionHandler(WalletNotFoundException.class)
    public ProblemDetail walletNotFound(WalletNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Wallet not found");
        return problem;
    }
}
