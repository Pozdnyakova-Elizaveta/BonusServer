package org.example.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Запрос на выполнение операции с бонусами - начисление или списание
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BonusOperationRequest {
    /**
     * Номер карты
     */
    @NotBlank
    @Pattern(regexp = "\\d+")
    private String cardNumber;
    /**
     * Количество бонусов
     */
    @NotNull
    @Positive
    private BigDecimal amountBonus;
}
