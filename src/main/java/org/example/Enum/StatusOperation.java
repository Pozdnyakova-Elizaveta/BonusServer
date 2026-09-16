package org.example.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Статус операции - выполена или отменена
 */
@Getter
@AllArgsConstructor
public enum StatusOperation {
    COMPLETED("Completed"),
    CANCELED("Canceled");

    private final String title;
}
