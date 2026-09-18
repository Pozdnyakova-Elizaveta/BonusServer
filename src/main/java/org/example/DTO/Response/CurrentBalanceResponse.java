package org.example.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO для передачи текущего баланса
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class CurrentBalanceResponse {
    /**
     * Номер карты
     */
    private String cardNumber;
    /**
     * Количество бонусов
     */
    private BigDecimal balance;
}
