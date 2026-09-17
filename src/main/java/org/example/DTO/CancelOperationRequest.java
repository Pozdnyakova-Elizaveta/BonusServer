package org.example.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Запрос на выполнение операции отмены
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CancelOperationRequest {
    /**
     * Идентификатор операции
     */
    @NotNull
    @Positive
    Long operationId;
}
