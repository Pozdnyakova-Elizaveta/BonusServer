package org.example.Service;

import lombok.AllArgsConstructor;
import org.example.DTO.BonusOperationRequest;
import org.example.DTO.CurrentBalanceDTO;
import org.example.DTO.BonusOperationDTO;
import org.example.Entity.BonusAccount;
import org.example.Entity.BonusOperation;
import org.example.Enum.StatusOperation;
import org.example.Enum.TypeOperation;
import org.example.Exception.*;
import org.example.Repository.BonusAccountRepository;
import org.example.Repository.BonusOperationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Реализация сервиса управления бонусами
 */
@Service
@AllArgsConstructor
public class BonusServiceImpl implements BonusService {
    private final BonusAccountRepository bonusAccountRepository;
    private final BonusOperationRepository bonusOperationRepository;

    /**
     * Начисление бонусов
     *
     * @param bonusOperationRequest dto-объект изменения баланса бонусов
     * @return dto-объект операции над бонусами
     */
    @Transactional
    @Override
    public BonusOperationDTO accrual(BonusOperationRequest bonusOperationRequest) {
        BonusAccount bonusAccount = bonusAccountRepository.findByCardNumber(bonusOperationRequest.getCardNumber())
                .orElseGet(() ->
                        bonusAccountRepository.save(BonusAccount.builder()
                                .cardNumber(bonusOperationRequest.getCardNumber())
                                .balance(bonusOperationRequest.getAmountBonus()).build()));
        BigDecimal balance = bonusAccount.getBalance();
        bonusAccount.setBalance(balance.add(bonusOperationRequest.getAmountBonus()));
        BonusOperation bonusOperation = saveOperation(bonusAccount.getId(), bonusOperationRequest.getAmountBonus(),
                TypeOperation.ACCRUAL, null);
        return toDto(bonusOperation);
    }

    /**
     * Списание бонусов
     *
     * @param bonusOperationRequest dto-объект изменения баланса бонусов
     * @return dto-объект операции над бонусами
     */
    @Transactional
    @Override
    public BonusOperationDTO deduction(BonusOperationRequest bonusOperationRequest) {
        String cardNumber = bonusOperationRequest.getCardNumber();
        BonusAccount bonusAccount = bonusAccountRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new AccountNotFoundException(cardNumber));
        BigDecimal balance = bonusAccount.getBalance();
        if (balance.compareTo(bonusOperationRequest.getAmountBonus()) < 0)
            throw new InsufficientBonusException(cardNumber, balance);
        bonusAccount.setBalance(balance.subtract(bonusOperationRequest.getAmountBonus()));
        BonusOperation bonusOperation = saveOperation(bonusAccount.getId(), bonusOperationRequest.getAmountBonus(),
                TypeOperation.DEDUCTION, null);
        return toDto(bonusOperation);
    }

    /**
     * Отмена операции
     *
     * @param idOperation идентификатор операции
     * @return dto-объект операции над бонусами
     */
    @Transactional
    @Override
    public BonusOperationDTO cancel(Long idOperation) {
        BonusOperation cancelOperation = bonusOperationRepository.findById(idOperation)
                .orElseThrow(() -> new OperationNotFoundException(idOperation));
        if (cancelOperation.getStatus() == StatusOperation.CANCELED) {
            throw new AlreadyCancelException(cancelOperation.getId());
        }
        Long idCustomer = cancelOperation.getIdAccount();
        BonusAccount bonusAccount = bonusAccountRepository.findById(idCustomer)
                .orElseThrow(() -> new AccountNotFoundException(idCustomer));
        BigDecimal balance = bonusAccount.getBalance();
        switch (cancelOperation.getTypeOperation()) {
            case ACCRUAL -> bonusAccount.setBalance(balance.subtract(cancelOperation.getAmountBonus()));
            case DEDUCTION -> bonusAccount.setBalance(balance.add(cancelOperation.getAmountBonus()));
            case CANCELLATION -> throw new UnacceptableCancelException(cancelOperation.getId());
        }
        cancelOperation.setStatus(StatusOperation.CANCELED);
        BonusOperation bonusOperation = saveOperation(idCustomer, cancelOperation.getAmountBonus(),
                TypeOperation.CANCELLATION, cancelOperation.getId());
        return toDto(bonusOperation);
    }

    /**
     * Получение баланса
     *
     * @param cardNumber номер карты
     * @return dto-объект текущего баланса
     */
    @Transactional(readOnly = true)
    @Override
    public CurrentBalanceDTO getBalance(String cardNumber) {
        BonusAccount bonusAccount = bonusAccountRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new AccountNotFoundException(cardNumber));
        return new CurrentBalanceDTO(cardNumber, bonusAccount.getBalance());
    }

    /**
     * Получение истории операций
     *
     * @param cardNumber номер карты
     * @param pageable   параметры пагинации
     * @return страница с операциями по данной карте
     */
    @Transactional(readOnly = true)
    @Override
    public Page<BonusOperationDTO> getHistory(String cardNumber, Pageable pageable) {
        BonusAccount bonusAccount = bonusAccountRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new AccountNotFoundException(cardNumber));
        return bonusOperationRepository.findByIdAccount(bonusAccount.getId(), pageable).map(this::toDto);
    }

    /**
     * Сохранение информации об операции
     *
     * @param accountId            идентификатор бонусного счета
     * @param amountBonus          количество бонусов, участвующих в операции
     * @param typeOperation        тип операции
     * @param idCancelledOperation для отмены - идентификатор отмененной операции
     * @return сущность операции
     */
    private BonusOperation saveOperation(Long accountId, BigDecimal amountBonus, TypeOperation typeOperation,
                                         Long idCancelledOperation) {
        BonusOperation bonusOperation = BonusOperation.builder()
                .idAccount(accountId)
                .amountBonus(amountBonus)
                .typeOperation(typeOperation)
                .status(StatusOperation.COMPLETED)
                .idCancelledOperation(idCancelledOperation)
                .creationAt(LocalDateTime.now())
                .build();
        return bonusOperationRepository.save(bonusOperation);
    }

    /**
     * Преобразование объекта-сущности операции в DTO
     *
     * @param bonusOperation объект-сущность
     * @return DTO-объект
     */
    private BonusOperationDTO toDto(BonusOperation bonusOperation) {
        return BonusOperationDTO.builder().id(bonusOperation.getId())
                .idAccount(bonusOperation.getIdAccount())
                .typeOperation(bonusOperation.getTypeOperation().getTitle())
                .amountBonus(bonusOperation.getAmountBonus())
                .idCancelledOperation(bonusOperation.getIdCancelledOperation())
                .creationAt(bonusOperation.getCreationAt()).build();
    }
}
