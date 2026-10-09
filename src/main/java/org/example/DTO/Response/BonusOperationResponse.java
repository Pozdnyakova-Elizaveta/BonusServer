package org.example.DTO.Response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO для передачи информации об операции
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BonusOperationResponse {
    /**
     * Идентификатор операции
     */
    private Long id;
    /**
     * Внешний ключ на счет
     */
    private Long accountId;
    /**
     * Тип операции: начисление, списание, возврат
     */
    private String typeOperation;
    /**
     * Для возврата списанных/начисленных бонусов - id отмененной операции
     */
    private Long cancelledOperationId;
    /**
     * Сумма бонусов, участвующих в операции
     */
    private BigDecimal amountBonus;
    /**
     * Статус операции: выполнена или отменена
     */
    private String statusOperation;
    /**
     * Дата и время создания операции
     */
    private LocalDateTime creationAt;
}
