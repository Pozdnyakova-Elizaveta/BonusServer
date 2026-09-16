package org.example.Exception;

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
}