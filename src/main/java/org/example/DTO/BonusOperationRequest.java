package org.example.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Digits;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

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
    @NotNull
    @Pattern(regexp = "\\d{16}")
    private String cardNumber;
    /**
     * Количество бонусов
     */
    @NotNull
    @Positive
    @Digits(integer = 19, fraction = 2)
    private BigDecimal amountBonus;
}
