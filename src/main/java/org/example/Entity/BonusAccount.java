package org.example.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Сущность бонусного счета
 */
@Entity
@Table(name = "bonus_account")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BonusAccount {
    /**
     * Идентификатор счета
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * Номер карты
     */
    @Pattern(regexp = "\\d{16}")
    @Column(name="card_number", nullable = false, unique = true)
    private String cardNumber;
    /**
     * Количество бонусов
     */
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal balance;
    /**
     * Версия для оптимистической блокировки
     */
    @Version
    @Column(nullable = false)
    private Long version;
}
