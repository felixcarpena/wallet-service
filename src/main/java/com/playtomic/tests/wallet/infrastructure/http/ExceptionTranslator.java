package com.playtomic.tests.wallet.infrastructure.http;

import com.playtomic.tests.wallet.domain.exceptions.CurrencyMismatchException;
import com.playtomic.tests.wallet.domain.exceptions.InvalidCardNumberException;
import com.playtomic.tests.wallet.domain.exceptions.NonPositiveAmountException;
import com.playtomic.tests.wallet.domain.exceptions.TooManyDecimalsException;
import com.playtomic.tests.wallet.domain.exceptions.WalletNotFoundException;
import com.playtomic.tests.wallet.infrastructure.http.exceptions.InvalidTopUpRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionTranslator {

    @ExceptionHandler({NonPositiveAmountException.class, CurrencyMismatchException.class,
            TooManyDecimalsException.class, InvalidCardNumberException.class, InvalidTopUpRequestException.class})
    public ProblemDetail invalidTopUp(RuntimeException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Invalid top-up");
        return problem;
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public ProblemDetail walletNotFound(WalletNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Wallet not found");
        return problem;
    }
}
