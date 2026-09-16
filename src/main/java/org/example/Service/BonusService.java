package org.example.Service;

import org.example.DTO.BonusOperationRequest;
import org.example.DTO.CurrentBalanceDTO;
import org.example.DTO.BonusOperationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Сервис управления бонусными счетами
 */
public interface BonusService {
    BonusOperationDTO accrual(BonusOperationRequest bonusOperationRequest);

    BonusOperationDTO deduction(BonusOperationRequest bonusOperationRequest);

    BonusOperationDTO cancel(Long idOperation);

    CurrentBalanceDTO getBalance(String cardNumber);

    Page<BonusOperationDTO> getHistory(String cardNumber, Pageable pageable);
}
