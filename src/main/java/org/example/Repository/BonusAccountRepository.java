package org.example.Repository;

import org.example.Entity.BonusAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для бонусных счетов
 */
@Repository
public interface BonusAccountRepository extends JpaRepository<BonusAccount, Long> {
    Optional<BonusAccount> findByCardNumber(String cardNumber);
}
