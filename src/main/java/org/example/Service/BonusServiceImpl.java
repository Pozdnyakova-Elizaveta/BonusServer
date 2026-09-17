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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final Logger log = LoggerFactory.getLogger(BonusServiceImpl.class);
    private static final String ACCOUNT_NOT_FOUND_BY_NUMBER="New bonus account record has been created: cardNumber={}";
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
                .orElseGet(() -> {
                    log.info("New bonus account record has been created: cardNumber={}",
                            bonusOperationRequest.getCardNumber());
                    return bonusAccountRepository.save(BonusAccount.builder()
                            .cardNumber(bonusOperationRequest.getCardNumber())
                            .balance(BigDecimal.ZERO).build());
                });
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
                .orElseThrow(() -> {
                    log.warn(ACCOUNT_NOT_FOUND_BY_NUMBER, cardNumber);
                    return new AccountNotFoundException(cardNumber);
                });
        BigDecimal balance = bonusAccount.getBalance();
        if (balance.compareTo(bonusOperationRequest.getAmountBonus()) < 0) {
            log.warn("Not enough bonuses for deduction: card number={}, on account={}, deduction={}", cardNumber,
                    balance, bonusOperationRequest.getAmountBonus());
            throw new InsufficientBonusException(cardNumber, balance);
        }
        bonusAccount.setBalance(balance.subtract(bonusOperationRequest.getAmountBonus()));
        BonusOperation bonusOperation = saveOperation(bonusAccount.getId(), bonusOperationRequest.getAmountBonus(),
                TypeOperation.DEDUCTION, null);
        return toDto(bonusOperation);
    }

    /**
     * Отмена операции
     *
     * @param operationId идентификатор операции
     * @return dto-объект операции над бонусами
     */
    @Transactional
    @Override
    public BonusOperationDTO cancel(Long operationId) {
        BonusOperation cancelOperation = bonusOperationRepository.findById(operationId)
                .orElseThrow(() -> {
                        log.warn("No operation with id: {}", operationId);
                    return new OperationNotFoundException(operationId);
                });
        if (cancelOperation.getStatus() == StatusOperation.CANCELED) {
            log.warn("Operation has already been cancelled: id={}, accountId={}, typeOperation={}, statusOperation={}",
                    cancelOperation.getId(), cancelOperation.getAccountId(), cancelOperation.getTypeOperation(),
                    cancelOperation.getStatus());
            throw new AlreadyCancelException(cancelOperation.getId());
        }
        Long idCustomer = cancelOperation.getAccountId();
        BonusAccount bonusAccount = bonusAccountRepository.findById(idCustomer)
                .orElseThrow(() -> {
                    log.warn("No account with id: {}", idCustomer);
                    return new AccountNotFoundException(idCustomer);
                });
        BigDecimal balance = bonusAccount.getBalance();
        switch (cancelOperation.getTypeOperation()) {
            case ACCRUAL -> {
                bonusAccount.setBalance(balance.subtract(cancelOperation.getAmountBonus()));
            }
            case DEDUCTION -> {
                bonusAccount.setBalance(balance.add(cancelOperation.getAmountBonus()));
            }
            case CANCELLATION -> {
                log.warn("An attempt to cancel a cancelled operation:  id={}, accountId={}, typeOperation={}, " +
                                "statusOperation={}",
                        cancelOperation.getId(), cancelOperation.getAccountId(), cancelOperation.getTypeOperation(),
                        cancelOperation.getStatus());
                throw new UnacceptableCancelException(cancelOperation.getId());
            }
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
                .orElseThrow(() -> {
                    log.warn(ACCOUNT_NOT_FOUND_BY_NUMBER, cardNumber);
                    return new AccountNotFoundException(cardNumber);
                });
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
                .orElseThrow(() -> {
                    log.warn(ACCOUNT_NOT_FOUND_BY_NUMBER, cardNumber);
                    return new AccountNotFoundException(cardNumber);
                });
        return bonusOperationRepository.findByAccountId(bonusAccount.getId(), pageable).map(this::toDto);
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
                .accountId(accountId)
                .amountBonus(amountBonus)
                .typeOperation(typeOperation)
                .status(StatusOperation.COMPLETED)
                .cancelledOperationId(idCancelledOperation)
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
                .accountId(bonusOperation.getAccountId())
                .typeOperation(bonusOperation.getTypeOperation().getTitle())
                .amountBonus(bonusOperation.getAmountBonus())
                .cancelledOperationId(bonusOperation.getCancelledOperationId())
                .statusOperation(bonusOperation.getStatus().getTitle())
                .creationAt(bonusOperation.getCreationAt()).build();
    }
}
