package org.example.Controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import org.example.DTO.Request.BonusOperationRequest;
import org.example.DTO.Response.CurrentBalanceResponse;
import org.example.DTO.Response.BonusOperationResponse;
import org.example.Service.Interface.BonusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/bonus-server")
@AllArgsConstructor
public class BonusController {
    private static final Logger log = LoggerFactory.getLogger(BonusController.class);
    private final BonusService bonusService;

    /**
     * Начисление бонусов
     *
     * @param bonusOperationRequest запрос с данными о карте и количестве начисляемых бонусов
     * @return DTO операции над бонусами
     */
    @PostMapping("/accrual")
    public BonusOperationResponse accrual(@Valid @RequestBody BonusOperationRequest bonusOperationRequest) {
        log.info("Bonus accrual: cardNumber={}, amountBonus={}",
                bonusOperationRequest.getCardNumber(), bonusOperationRequest.getAmountBonus());
        BonusOperationResponse response = bonusService.accrual(bonusOperationRequest);
        log.info("Bonuses have been accrued: id={}, accountId={}, typeOperation={}," +
                        "amountBonus={}, statusOperation={}, creationAt={}",
                response.getId(), response.getAccountId(), response.getTypeOperation(), response.getAmountBonus(),
                response.getStatusOperation(), response.getCreationAt());
        return response;
    }

    /**
     * Списание бонусов
     *
     * @param bonusOperationRequest запрос с данными о карте и количестве списываемых бонусов
     * @return DTO операции над бонусами
     */
    @PostMapping("/deduction")
    public BonusOperationResponse deduction(@Valid @RequestBody BonusOperationRequest bonusOperationRequest) {
        log.info("Bonus deduction: cardNumber={}, amountBonus={}",
                bonusOperationRequest.getCardNumber(), bonusOperationRequest.getAmountBonus());
        BonusOperationResponse response = bonusService.deduction(bonusOperationRequest);
        log.info("Bonuses have been deducted: id={}, accountId={}, typeOperation={}," +
                        "amountBonus={}, statusOperation={}, creationAt={}",
                response.getId(), response.getAccountId(), response.getTypeOperation(), response.getAmountBonus(),
                response.getStatusOperation(), response.getCreationAt());
        return response;
    }

    /**
     * Отмена операции
     *
     * @param operationId идентификатор отменяемой операции
     * @return DTO операции над бонусами
     */
    @PostMapping("/operations/{operationId}/cancel")
    public BonusOperationResponse cancel(@PathVariable @Positive Long operationId) {
        log.info("Cancellation of  operation: operationId={}", operationId);
        BonusOperationResponse response = bonusService.cancel(operationId);
        log.info("Operation has been cancelled: id={}, accountId={}, typeOperation={}," +
                        "amountBonus={}, statusOperation={}, creationAt={}",
                response.getId(), response.getAccountId(), response.getTypeOperation(), response.getAmountBonus(),
                response.getStatusOperation(), response.getCreationAt());
        return response;
    }

    /**
     * Получение истории операций с использованием пагинации
     *
     * @param cardNumber номер карты
     * @param pageable   параметры пагинации
     * @return страница с операциями
     */
    @GetMapping("/history")
    public Page<BonusOperationResponse> getHistory(@NotNull @Pattern(regexp = "\\d{16}") @RequestParam("cardNumber") String cardNumber,
                                                   @PageableDefault(size = 20, sort = {"creationAt", "id"},
                                                   direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Getting bonus history: cardNumber={}, pageSize={}, pageNumber={}",
                cardNumber, pageable.getPageSize(), pageable.getPageNumber());
        Page<BonusOperationResponse> response = bonusService.getHistory(cardNumber, pageable);
        log.info("Bonus history has been got: cardNumber={}, totalElements={}, totalPages={}",
                cardNumber, response.getTotalElements(), response.getTotalPages());
        return response;
    }

    /**
     * Получение баланса
     *
     * @param cardNumber номер карты
     * @return DTO-объект текущего баланса
     */
    @GetMapping(path = "/balance")
    public CurrentBalanceResponse getBalance(@NotNull @Pattern(regexp = "\\d{16}") @RequestParam("cardNumber") String cardNumber) {
        log.info("Getting bonus balance: cardNumber={}", cardNumber);
        CurrentBalanceResponse response = bonusService.getBalance(cardNumber);
        log.info("Bonus balance has been got: cardNumber={}, balance={}", response.getCardNumber(), response.getBalance());
        return response;
    }


}
