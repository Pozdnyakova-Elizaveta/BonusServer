package org.example.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.Enum.StatusOperation;
import org.example.Enum.TypeOperation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Сущность операции над бонусами
 */
@Entity
@Table(name = "bonus_operation")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BonusOperation {
    /**
     * Идентификатор операции
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * Внешний ключ на счет
     */
    @Column(name = "account_id", nullable = false)
    private Long accountId;
    /**
     * Тип операции: начисление, списание, возврат
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type_operation", nullable = false)
    private TypeOperation typeOperation;
    /**
     * Для возврата списанных/начисленных бонусов - id отмененной операции
     */
    @Column(name = "cancelled_operation_id")
    private Long cancelledOperationId;
    /**
     * Сумма бонусов, участвующих в операции
     */
    @Column(name = "amount_bonus", nullable = false)
    private BigDecimal amountBonus;
    /**
     * Статус операции: выполнена или отменена
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOperation status;
    /**
     * Дата и время создания операции
     */
    @Column(name = "creation_at", nullable = false)
    private LocalDateTime creationAt;
}
