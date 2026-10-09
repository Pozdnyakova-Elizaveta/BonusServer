package org.example.Exception;

import org.springframework.http.HttpStatus;

import java.math.BigDecimal;

/**
 * Исключение - недостаточно средств для снятия бонусов
 */
public class InsufficientBonusException extends ApplicationException {
    public InsufficientBonusException(String cardNumber, BigDecimal balance) {
        super(String.format("Failed to deduct bonuses from card %s, balance: %s", cardNumber, balance));
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.UNPROCESSABLE_CONTENT;
    }
}
