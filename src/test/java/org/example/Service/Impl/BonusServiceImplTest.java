package org.example.Service.Impl;

import org.example.DTO.Request.BonusOperationRequest;
import org.example.DTO.Response.BonusOperationResponse;
import org.example.DTO.Response.CurrentBalanceResponse;
import org.example.Entity.BonusAccount;
import org.example.Entity.BonusOperation;
import org.example.Enum.StatusOperation;
import org.example.Enum.TypeOperation;
import org.example.Exception.*;
import org.example.Repository.BonusAccountRepository;
import org.example.Repository.BonusOperationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Класс тестирования сервиса управления бонусами
 */
@ExtendWith(MockitoExtension.class)
class BonusServiceImplTest {

    /**
     * Тестовые значения
     */
    private static final String CARD_NUMBER = "1234567890123456";
    private static final BigDecimal AMOUNT = new BigDecimal("50.00");
    private static final BigDecimal BALANCE = new BigDecimal("100.00");
    private static final String NULL_CARD_NUMBER = "0000000000000000";

    /**
     * Объект-заглушка для счетов с бонусами
     */
    @Mock
    private BonusAccountRepository bonusAccountRepository;

    /**
     * Объект-заглушка для репозитория операций по бонусам
     */
    @Mock
    private BonusOperationRepository bonusOperationRepository;

    /**
     * Объект сервиса для бонусных счетов с репозиториями-заглушками
     */
    @InjectMocks
    private BonusServiceImpl bonusService;

    /**
     * Сущности для тестирования
     */
    private BonusAccount bonusAccount;
    private BonusOperation bonusOperationAccrual;
    private BonusOperation bonusOperationDeduction;
    private BonusOperationRequest accrualRequest;
    private BonusOperation cancellationOp;

    /**
     * Создание тестовых объектов перед каждым тестом
     */
    @BeforeEach
    void setUp() {
        bonusAccount = BonusAccount.builder()
                .id(1L)
                .cardNumber(CARD_NUMBER)
                .balance(BALANCE)
                .version(0L)
                .build();
        bonusOperationAccrual = BonusOperation.builder()
                .id(1L)
                .accountId(1L)
                .typeOperation(TypeOperation.ACCRUAL)
                .amountBonus(AMOUNT)
                .status(StatusOperation.COMPLETED)
                .creationAt(LocalDateTime.of(2025, 1, 1, 10, 0))
                .cancelledOperationId(null)
                .version(0L)
                .build();
        bonusOperationDeduction = BonusOperation.builder()
                .id(2L)
                .accountId(1L)
                .typeOperation(TypeOperation.DEDUCTION)
                .amountBonus(AMOUNT)
                .status(StatusOperation.COMPLETED)
                .creationAt(LocalDateTime.of(2025, 1, 1, 10, 0))
                .cancelledOperationId(null)
                .version(0L)
                .build();
        accrualRequest = new BonusOperationRequest(CARD_NUMBER, AMOUNT);
        cancellationOp = BonusOperation.builder()
                .id(3L)
                .accountId(1L)
                .typeOperation(TypeOperation.CANCELLATION)
                .amountBonus(AMOUNT)
                .status(StatusOperation.COMPLETED)
                .creationAt(LocalDateTime.of(2025, 1, 1, 11, 0))
                .cancelledOperationId(1L)
                .version(0L)
                .build();
    }

    /**
     * Тест успешного начисления бонусов на существующий счет
     */
    @Test
    void testAccrualToExistingAccount() {
        when(bonusAccountRepository.findByCardNumber(CARD_NUMBER))
                .thenReturn(Optional.of(bonusAccount));
        when(bonusOperationRepository.save(any(BonusOperation.class))).thenReturn(bonusOperationAccrual);
        BonusOperationResponse response = bonusService.accrual(accrualRequest);
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(TypeOperation.ACCRUAL.getTitle(), response.getTypeOperation());
        assertEquals(AMOUNT, response.getAmountBonus());
        assertEquals("Completed", response.getStatusOperation());
        assertEquals(BALANCE.add(AMOUNT), bonusAccount.getBalance());
        verify(bonusAccountRepository).findByCardNumber(CARD_NUMBER);
        verify(bonusOperationRepository, times(1)).save(any(BonusOperation.class));
    }

    /**
     * Тест создания счета и начисления бонусов при отсутствии счета
     */
    @Test
    void testCreateAccountAndAccrualWhenNotFound() {
        when(bonusAccountRepository.findByCardNumber(CARD_NUMBER))
                .thenReturn(Optional.empty());
        when(bonusAccountRepository.save(any(BonusAccount.class))).thenReturn(bonusAccount);
        when(bonusOperationRepository.save(any(BonusOperation.class))).thenReturn(bonusOperationAccrual);
        BonusOperationResponse response = bonusService.accrual(accrualRequest);
        assertNotNull(response);
        assertEquals(TypeOperation.ACCRUAL.getTitle(), response.getTypeOperation());
        verify(bonusAccountRepository).findByCardNumber(CARD_NUMBER);
        verify(bonusAccountRepository).save(any(BonusAccount.class));
        verify(bonusOperationRepository, times(1)).save(any(BonusOperation.class));
    }

    /**
     * Тест успешного списания бонусов
     */
    @Test
    void testSuccessfulDeduction() {
        BonusOperationRequest deductionRequest = new BonusOperationRequest(CARD_NUMBER, new BigDecimal("30.00"));
        when(bonusAccountRepository.findByCardNumber(CARD_NUMBER))
                .thenReturn(Optional.of(bonusAccount));
        when(bonusOperationRepository.save(any(BonusOperation.class))).thenReturn(bonusOperationDeduction);
        BonusOperationResponse response = bonusService.deduction(deductionRequest);
        assertNotNull(response);
        assertEquals(TypeOperation.DEDUCTION.getTitle(), response.getTypeOperation());
        assertEquals(new BigDecimal("70.00"), bonusAccount.getBalance());
        verify(bonusAccountRepository).findByCardNumber(CARD_NUMBER);
        verify(bonusOperationRepository, times(1)).save(any(BonusOperation.class));
    }

    /**
     * Тест - выброс исключения при отсутствии счета для списания
     */
    @Test
    void testDeductionExceptionWhenAccountNotFound() {
        BonusOperationRequest deductionRequest = new BonusOperationRequest(NULL_CARD_NUMBER, new BigDecimal("30.00"));
        when(bonusAccountRepository.findByCardNumber(NULL_CARD_NUMBER))
                .thenReturn(Optional.empty());
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> {
            bonusService.deduction(deductionRequest);
        });
        assertTrue(exception.getMessage().contains(NULL_CARD_NUMBER));
        verify(bonusOperationRepository, never()).save(any());
    }

    /**
     * Тест - выброс исключения при недостаточном балансе для списания
     */
    @Test
    void testDeductionExceptionWhenInsufficientBalance() {
        BonusOperationRequest deductionRequest = new BonusOperationRequest(CARD_NUMBER, new BigDecimal("150.00"));
        when(bonusAccountRepository.findByCardNumber(CARD_NUMBER))
                .thenReturn(Optional.of(bonusAccount));
        InsufficientBonusException exception = assertThrows(InsufficientBonusException.class, () -> {
            bonusService.deduction(deductionRequest);
        });
        assertTrue(exception.getMessage().contains(CARD_NUMBER));
        verify(bonusOperationRepository, never()).save(any());
    }

    /**
     * Тест списания всей суммы при точном совпадении баланса
     */
    @Test
    void testDeductionEntireBalance() {
        BonusOperationRequest deductionRequest = new BonusOperationRequest(CARD_NUMBER, BALANCE);
        when(bonusAccountRepository.findByCardNumber(CARD_NUMBER))
                .thenReturn(Optional.of(bonusAccount));
        when(bonusOperationRepository.save(any(BonusOperation.class))).thenReturn(bonusOperationDeduction);
        BonusOperationResponse response = bonusService.deduction(deductionRequest);
        assertNotNull(response);
        assertEquals(new BigDecimal("0.00"), bonusAccount.getBalance());
    }

    /**
     * Тест отмены операции начисления
     */
    @Test
    void testCancelAccrualOperation() {
        when(bonusOperationRepository.findById(1L)).thenReturn(Optional.of(bonusOperationAccrual));
        when(bonusAccountRepository.findById(1L)).thenReturn(Optional.of(bonusAccount));
        when(bonusOperationRepository.save(any(BonusOperation.class)))
                .thenReturn(cancellationOp);
        BigDecimal resultBalance = bonusAccount.getBalance().subtract(bonusOperationAccrual.getAmountBonus());
        BonusOperationResponse response = bonusService.cancel(1L);
        assertNotNull(response);
        assertEquals(TypeOperation.CANCELLATION.getTitle(), response.getTypeOperation());
        assertEquals(1L, response.getCancelledOperationId());
        assertEquals("Completed", response.getStatusOperation());
        assertEquals(bonusOperationAccrual.getAmountBonus(), response.getAmountBonus());
        assertEquals(resultBalance, bonusAccount.getBalance());
        assertEquals(StatusOperation.CANCELLED, bonusOperationAccrual.getStatus());
    }

    /**
     * Тест отмены операции списания
     */
    @Test
    void testCancelDeductionOperation() {
        cancellationOp.setCancelledOperationId(2L);
        when(bonusOperationRepository.findById(2L)).thenReturn(Optional.of(bonusOperationDeduction));
        when(bonusAccountRepository.findById(1L)).thenReturn(Optional.of(bonusAccount));
        when(bonusOperationRepository.save(any(BonusOperation.class))).thenReturn(cancellationOp);
        BigDecimal resultBalance = bonusAccount.getBalance().add(bonusOperationDeduction.getAmountBonus());
        BonusOperationResponse response = bonusService.cancel(2L);
        assertNotNull(response);
        assertEquals(TypeOperation.CANCELLATION.getTitle(), response.getTypeOperation());
        assertEquals(2L, response.getCancelledOperationId());
        assertEquals("Completed", response.getStatusOperation());
        assertEquals(bonusOperationDeduction.getAmountBonus(), response.getAmountBonus());
        assertEquals(resultBalance, bonusAccount.getBalance());
        assertEquals(StatusOperation.CANCELLED, bonusOperationDeduction.getStatus());
    }

    /**
     * Тест - выброс исключения при отсутствии операции для отмены
     */
    @Test
    void testCancelExceptionWhenOperationNotFound() {
        when(bonusOperationRepository.findById(999L)).thenReturn(Optional.empty());
        OperationNotFoundException exception = assertThrows(OperationNotFoundException.class, () -> {
            bonusService.cancel(999L);
        });
        assertTrue(exception.getMessage().contains("999"));
    }

    /**
     * Тест - выброс исключения при отмене уже отмененной операции
     */
    @Test
    void testCancelExceptionWhenAlreadyCanceled() {
        BonusOperation canceledOp = BonusOperation.builder()
                .id(1L)
                .accountId(1L)
                .typeOperation(TypeOperation.ACCRUAL)
                .amountBonus(AMOUNT)
                .status(StatusOperation.CANCELLED)
                .creationAt(LocalDateTime.of(2025, 1, 1, 10, 0))
                .cancelledOperationId(null)
                .version(0L)
                .build();
        when(bonusOperationRepository.findById(1L)).thenReturn(Optional.of(canceledOp));
        OperationAlreadyCanceledException exception = assertThrows(OperationAlreadyCanceledException.class, () -> {
            bonusService.cancel(1L);
        });
        assertTrue(exception.getMessage().contains("1"));
    }

    /**
     * Тест - выброс исключения при попытке отменить операцию отмены
     */
    @Test
    void testCancelExceptionWhenCancelingCancellationOperation() {
        when(bonusOperationRepository.findById(3L)).thenReturn(Optional.of(cancellationOp));
        when(bonusAccountRepository.findById(1L)).thenReturn(Optional.of(bonusAccount));
        UnacceptableCancelException exception = assertThrows(UnacceptableCancelException.class, () -> {
            bonusService.cancel(3L);
        });
        assertTrue(exception.getMessage().contains("3"));
    }

    /**
     * Тест - выброс исключения при отсутствии счета для отменяемой операции
     */
    @Test
    void testCancelExceptionWhenAccountNotFoundForOperation() {
        BonusOperation operation = BonusOperation.builder()
                .id(1L)
                .accountId(999L)
                .typeOperation(TypeOperation.ACCRUAL)
                .amountBonus(AMOUNT)
                .status(StatusOperation.COMPLETED)
                .creationAt(LocalDateTime.of(2025, 1, 1, 10, 0))
                .cancelledOperationId(null)
                .version(0L)
                .build();
        when(bonusOperationRepository.findById(1L)).thenReturn(Optional.of(operation));
        when(bonusAccountRepository.findById(999L)).thenReturn(Optional.empty());
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> {
            bonusService.cancel(1L);
        });
        assertTrue(exception.getMessage().contains("999"));
    }

    /**
     * Тест получения баланса существующего счета
     */
    @Test
    void testGetBalanceForExistingAccount() {
        when(bonusAccountRepository.findByCardNumber(CARD_NUMBER))
                .thenReturn(Optional.of(bonusAccount));
        CurrentBalanceResponse response = bonusService.getBalance(CARD_NUMBER);
        assertNotNull(response);
        assertEquals(CARD_NUMBER, response.getCardNumber());
        assertEquals(BALANCE, response.getBalance());
    }

    /**
     * Тест - выброс исключения при отсутствии счета для получения баланса
     */
    @Test
    void testGetBalanceExceptionWhenAccountNotFound() {
        when(bonusAccountRepository.findByCardNumber(NULL_CARD_NUMBER))
                .thenReturn(Optional.empty());
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> {
            bonusService.getBalance(NULL_CARD_NUMBER);
        });
        assertTrue(exception.getMessage().contains(NULL_CARD_NUMBER));
    }

    /**
     * Тест получения пагинированной истории операций
     */
    @Test
    void testGetHistoryPaginatedOperations() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<BonusOperation> operationPage = new PageImpl<>(List.of(bonusOperationAccrual));
        when(bonusAccountRepository.findByCardNumber(CARD_NUMBER))
                .thenReturn(Optional.of(bonusAccount));
        when(bonusOperationRepository.findByAccountId(1L, pageable))
                .thenReturn(operationPage);
        Page<BonusOperationResponse> response = bonusService.getHistory(CARD_NUMBER, pageable);
        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals("Accrual", response.getContent().getFirst().getTypeOperation());
        assertEquals("Completed", response.getContent().getFirst().getStatusOperation());
    }

    /**
     * Тест - выброс исключения при отсутствии счета для получения истории
     */
    @Test
    void testGetHistoryExceptionWhenAccountNotFound() {
        Pageable pageable = PageRequest.of(0, 10);
        when(bonusAccountRepository.findByCardNumber(NULL_CARD_NUMBER))
                .thenReturn(Optional.empty());
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> {
            bonusService.getHistory(NULL_CARD_NUMBER, pageable);
        });
        assertTrue(exception.getMessage().contains(NULL_CARD_NUMBER));
    }

    /**
     * Тест возврата пустой страницы при отсутствии операций
     */
    @Test
    void testGetHistoryEmptyPageWhenNoOperations() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<BonusOperation> emptyPage = Page.empty();
        when(bonusAccountRepository.findByCardNumber(CARD_NUMBER))
                .thenReturn(Optional.of(bonusAccount));
        when(bonusOperationRepository.findByAccountId(1L, pageable))
                .thenReturn(emptyPage);
        Page<BonusOperationResponse> response = bonusService.getHistory(CARD_NUMBER, pageable);
        assertNotNull(response);
        assertTrue(response.isEmpty());
        assertEquals(0, response.getTotalElements());
    }
}
