package org.example.Controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import org.example.DTO.BonusOperationRequest;
import org.example.DTO.CurrentBalanceDTO;
import org.example.DTO.BonusOperationDTO;
import org.example.Service.BonusService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("bonus_server/")
@AllArgsConstructor
public class RestController {
    private final BonusService bonusService;

    /**
     * Начисление бонусов
     *
     * @param bonusOperationRequest запрос с данными о карте и количестве начисляемых бонусов
     * @return DTO операции над бонусами
     */
    @PostMapping("/accrual")
    public BonusOperationDTO accrual(@Valid @RequestBody BonusOperationRequest bonusOperationRequest) {
        return bonusService.accrual(bonusOperationRequest);
    }

    /**
     * Списание бонусов
     *
     * @param bonusOperationRequest запрос с данными о карте и количестве списываемых бонусов
     * @return DTO операции над бонусами
     */
    @PostMapping("/deduction")
    public BonusOperationDTO deduction(@Valid @RequestBody BonusOperationRequest bonusOperationRequest) {
        return bonusService.deduction(bonusOperationRequest);
    }

    /**
     * Отмена операции
     *
     * @param idOperation идентификатор отменяемой операции
     * @return DTO операции над бонусами
     */
    @PostMapping("/cancel")
    public BonusOperationDTO cancel(@NotNull @Positive @RequestBody Long idOperation) {
        return bonusService.cancel(idOperation);
    }

    /**
     * Получение истории операций с использованием пагинации
     *
     * @param cardNumber номер карты
     * @param pageable   параметры пагинации
     * @return страница с операциями
     */
    @GetMapping("/{cardNumber}/history")
    public Page<BonusOperationDTO> history(@NotBlank @Pattern(regexp = "\\d+") @PathVariable String cardNumber,
                                           @PageableDefault(size = 20, sort = "createdAt",
                                                   direction = Sort.Direction.DESC) Pageable pageable) {
        return bonusService.getHistory(cardNumber, pageable);
    }

    /**
     * Получение баланса
     *
     * @param cardNumber номер карты
     * @return DTO-объект текущего баланса
     */
    @GetMapping(path = "/balance")
    public CurrentBalanceDTO getBalance(@NotBlank @Pattern(regexp = "\\d+") @RequestParam String cardNumber) {
        return bonusService.getBalance(cardNumber);
    }


}
