package org.example.Service.Interface;

import org.example.DTO.Request.BonusOperationRequest;
import org.example.DTO.Response.CurrentBalanceResponse;
import org.example.DTO.Response.BonusOperationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Сервис управления бонусными счетами
 */
public interface BonusService {
    BonusOperationResponse accrual(BonusOperationRequest bonusOperationRequest);

    BonusOperationResponse deduction(BonusOperationRequest bonusOperationRequest);

    BonusOperationResponse cancel(Long operationId);

    CurrentBalanceResponse getBalance(String cardNumber);

    Page<BonusOperationResponse> getHistory(String cardNumber, Pageable pageable);
}
