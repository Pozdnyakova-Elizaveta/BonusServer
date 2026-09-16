package org.example.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO для передачи информации об операции
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
public class BonusOperationDTO {
    /**
     * Идентификатор операции
     */
    private Long id;
    /**
     * Внешний ключ на счет
     */
    private Long idAccount;
    /**
     * Тип операции: начисление, списание, возврат
     */
    private String typeOperation;
    /**
     * Для возврата списанных/начисленных бонусов - id отмененной операции
     */
    private Long idCancelledOperation;
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
