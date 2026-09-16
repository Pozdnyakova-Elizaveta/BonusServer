package org.example.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Тип операции - начисление, списание, отмена
 */
@Getter
@AllArgsConstructor
public enum TypeOperation {
    ACCRUAL("Accrual"),
    DEDUCTION("Deduction"),
    CANCELLATION("Cancellation");

    private final String title;
}
