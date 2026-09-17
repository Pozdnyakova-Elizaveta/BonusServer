package org.example.Controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import org.example.DTO.BonusOperationRequest;
import org.example.DTO.CancelOperationRequest;
import org.example.DTO.CurrentBalanceDTO;
import org.example.DTO.BonusOperationDTO;
import org.example.Service.BonusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/bonus_server")
@AllArgsConstructor
public class BonusController {
    private final Logger log = LoggerFactory.getLogger(BonusController.class);
    private final BonusService bonusService;

    /**
     * Начисление бонусов
     *
     * @param bonusOperationRequest запрос с данными о карте и количестве начисляемых бонусов
     * @return DTO операции над бонусами
     */
    @PostMapping("/accrual")
    @ResponseStatus(HttpStatus.CREATED)
    public BonusOperationDTO accrual(@Valid @RequestBody BonusOperationRequest bonusOperationRequest) {
        log.info("Bonus accrual: cardNumber={}, amountBonus={}",
                bonusOperationRequest.getCardNumber(), bonusOperationRequest.getAmountBonus());
        BonusOperationDTO response = bonusService.accrual(bonusOperationRequest);
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
    @ResponseStatus(HttpStatus.CREATED)
    public BonusOperationDTO deduction(@Valid @RequestBody BonusOperationRequest bonusOperationRequest) {
        log.info("Bonus deduction: cardNumber={}, amountBonus={}",
                bonusOperationRequest.getCardNumber(), bonusOperationRequest.getAmountBonus());
        BonusOperationDTO response = bonusService.deduction(bonusOperationRequest);
        log.info("Bonuses have been deducted: id={}, accountId={}, typeOperation={}," +
                        "amountBonus={}, statusOperation={}, creationAt={}",
                response.getId(), response.getAccountId(), response.getTypeOperation(), response.getAmountBonus(),
                response.getStatusOperation(), response.getCreationAt());
        return response;
    }

    /**
     * Отмена операции
     *
     * @param cancelOperationRequest запрос с идентификатором отменяемой операции
     * @return DTO операции над бонусами
     */
    @PostMapping("/cancel")
    @ResponseStatus(HttpStatus.CREATED)
    public BonusOperationDTO cancel(@Valid @RequestBody CancelOperationRequest cancelOperationRequest) {
        log.info("Cancellation of  operation: operationId={}",
                cancelOperationRequest.getOperationId());
        BonusOperationDTO response = bonusService.cancel(cancelOperationRequest.getOperationId());
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
    public Page<BonusOperationDTO> getHistory(@NotNull @Pattern(regexp = "\\d{16}") @RequestParam("cardNumber") String cardNumber,
                                              @PageableDefault(size = 20, sort = "createdAt",
                                                   direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("Getting bonus history: cardNumber={}, pageSize={}, pageNumber={}",
                cardNumber, pageable.getPageSize(), pageable.getPageNumber());
        Page<BonusOperationDTO> response = bonusService.getHistory(cardNumber, pageable);
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
    public CurrentBalanceDTO getBalance(@NotNull @Pattern(regexp = "\\d{16}") @RequestParam("cardNumber") String cardNumber) {
        log.info("Getting bonus balance: cardNumber={}", cardNumber);
        CurrentBalanceDTO response = bonusService.getBalance(cardNumber);
        log.info("Bonus balance has been got: cardNumber={}, balance={}", response.getCardNumber(), response.getBalance());
        return response;
    }


}
