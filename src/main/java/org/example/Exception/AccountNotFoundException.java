package org.example.Exception;

import org.springframework.http.HttpStatus;

/**
 * Исключение - счет не найден
 */
public class AccountNotFoundException extends ApplicationException {
    public AccountNotFoundException(String cardNumber) {
        super("Bonus account not found by card number: " + cardNumber);
    }

    public AccountNotFoundException(Long id) {
        super("Bonus account not found by id: " + id);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}