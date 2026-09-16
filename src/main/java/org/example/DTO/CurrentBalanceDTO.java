package org.example.DTO;

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
public class CurrentBalanceDTO {
    /**
     * Номер карты
     */
    private String cardNumber;
    /**
     * Количество бонусов
     */
    private BigDecimal balance;
}
