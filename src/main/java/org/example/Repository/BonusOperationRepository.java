package org.example.Repository;

import org.example.Entity.BonusOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для бонусных операций
 */
public interface BonusOperationRepository extends JpaRepository<BonusOperation, Long> {
    Page<BonusOperation> findByAccountId(Long accountId, Pageable pageable);
}
