package org.example.DTO.Response;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO для передачи текущего баланса
 */
@Getter
@NoArgsConstructor
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
