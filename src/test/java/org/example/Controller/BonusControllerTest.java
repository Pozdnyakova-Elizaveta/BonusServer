package org.example.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.DTO.Request.BonusOperationRequest;
import org.example.DTO.Response.BonusOperationResponse;
import org.example.DTO.Response.CurrentBalanceResponse;
import org.example.Service.Interface.BonusService;
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
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Класс тестирования контроллера по управлению бонусами
 */
@ExtendWith(MockitoExtension.class)
class BonusControllerTest {
    /**
     * Мок-объект для имитации http-запросов
     */
    private MockMvc mockMvc;
    /**
     * Мок-объект сервиса для бонусных счетов
     */
    @Mock
    private BonusService bonusService;
    /**
     * Объект контроллера с сервисами-заглушками
     */
    @InjectMocks
    private BonusController bonusController;
    /**
     * Маппер
     */
    private ObjectMapper objectMapper = new ObjectMapper();
    /**
     * Тестовые запросы
     */
    private BonusOperationRequest accrualRequest;
    private BonusOperationRequest deductionRequest;
    /**
     * Тестовые ответы
     */
    private BonusOperationResponse accrualResponse;
    private BonusOperationResponse deductionResponse;
    private CurrentBalanceResponse balanceResponse;

    /**
     * Тестовые значения
     */
    private static final String NUMBER_CARD = "1234567890123456";
    private static final String ACCRUAL = "Accrual";
    private static final String DEDUCTION = "Deduction";
    private static final String COMPLETED = "Completed";
    private static final BigDecimal ACCRUAL_AMOUNT = new BigDecimal("100.0");
    private static final BigDecimal DEDUCTION_AMOUNT = new BigDecimal("50.0");
    private static final BigDecimal BALANCE = new BigDecimal("150.0");

    /**
     * Создание тестовых объектов перед каждым тестом
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bonusController)
                .setControllerAdvice(new RestExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        accrualRequest = new BonusOperationRequest(NUMBER_CARD, ACCRUAL_AMOUNT);
        deductionRequest = new BonusOperationRequest(NUMBER_CARD, DEDUCTION_AMOUNT);

        accrualResponse = BonusOperationResponse.builder()
                .id(1L)
                .accountId(1L)
                .typeOperation(ACCRUAL)
                .amountBonus(ACCRUAL_AMOUNT)
                .statusOperation(COMPLETED)
                .creationAt(LocalDateTime.of(2025, 1, 1, 10, 0))
                .cancelledOperationId(null)
                .build();

        deductionResponse = BonusOperationResponse.builder()
                .id(2L)
                .accountId(1L)
                .typeOperation(DEDUCTION)
                .amountBonus(DEDUCTION_AMOUNT)
                .statusOperation(COMPLETED)
                .creationAt(LocalDateTime.of(2025, 1, 1, 11, 0))
                .cancelledOperationId(null)
                .build();

        balanceResponse = new CurrentBalanceResponse(NUMBER_CARD, BALANCE);
    }

    /**
     * Тест - пополнение счета с возратом записи операции
     */
    @Test
    void testAccrualReturnOperationResponse() throws Exception {
        when(bonusService.accrual(any(BonusOperationRequest.class))).thenReturn(accrualResponse);
        mockMvc.perform(post("/bonus-server/accrual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accrualRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.typeOperation").value(ACCRUAL))
                .andExpect(jsonPath("$.amountBonus").value(ACCRUAL_AMOUNT))
                .andExpect(jsonPath("$.statusOperation").value(COMPLETED));

        verify(bonusService).accrual(any(BonusOperationRequest.class));
    }

    /**
     * Тест - списание бонусов со счета с возратом записи операции
     */
    @Test
    void testDeductionReturnOperationResponse() throws Exception {
        when(bonusService.deduction(any(BonusOperationRequest.class))).thenReturn(deductionResponse);
        mockMvc.perform(post("/bonus-server/deduction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deductionRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.typeOperation").value(DEDUCTION))
                .andExpect(jsonPath("$.amountBonus").value(DEDUCTION_AMOUNT))
                .andExpect(jsonPath("$.statusOperation").value(COMPLETED));
        verify(bonusService).deduction(any(BonusOperationRequest.class));
    }

    /**
     * Тест - отмена операции начисления с возратом записи операции
     */
    @Test
    void testCancelReturnOperationResponse() throws Exception {
        BonusOperationResponse cancelResponse = BonusOperationResponse.builder()
                .id(3L)
                .accountId(1L)
                .typeOperation("Cancellation")
                .amountBonus(ACCRUAL_AMOUNT)
                .statusOperation(COMPLETED)
                .creationAt(LocalDateTime.of(2025, 1, 1, 12, 0))
                .cancelledOperationId(1L)
                .build();
        when(bonusService.cancel(1L)).thenReturn(cancelResponse);
        mockMvc.perform(post("/bonus-server/operations/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.typeOperation").value("Cancellation"))
                .andExpect(jsonPath("$.cancelledOperationId").value(1));
        verify(bonusService).cancel(1L);
    }

    /**
     * Тест - получение истории операций с пагинацией
     */
    @Test
    void testHistoryReturnPaginatedOperations() throws Exception {
        List<BonusOperationResponse> operations = List.of(accrualResponse, deductionResponse);
        Pageable pageable = PageRequest.of(0, 10);
        Page<BonusOperationResponse> page = new PageImpl<BonusOperationResponse>(operations, pageable, operations.size());
        when(bonusService.getHistory(eq(NUMBER_CARD), any(Pageable.class)))
                .thenReturn(page);
        mockMvc.perform(get("/bonus-server/history")
                        .param("cardNumber", NUMBER_CARD)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].typeOperation").value(ACCRUAL))
                .andExpect(jsonPath("$.content[1].typeOperation").value(DEDUCTION))
                .andExpect(jsonPath("$.totalElements").value(2));
        verify(bonusService).getHistory(eq(NUMBER_CARD), any(Pageable.class));
    }

    /**
     * Тест - получение пустой страницы истории
     */
    @Test
    void testHistoryReturnEmptyPage() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<BonusOperationResponse> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(bonusService.getHistory(eq(NUMBER_CARD), any(Pageable.class)))
                .thenReturn(emptyPage);
        mockMvc.perform(get("/bonus-server/history")
                        .param("cardNumber", NUMBER_CARD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    /**
     * Тест - получение баланса
     */
    @Test
    void testBalanceReturnCurrentBalance() throws Exception {
        when(bonusService.getBalance(NUMBER_CARD)).thenReturn(balanceResponse);
        mockMvc.perform(get("/bonus-server/balance")
                        .param("cardNumber", NUMBER_CARD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardNumber").value(NUMBER_CARD))
                .andExpect(jsonPath("$.balance").value(BALANCE));
        verify(bonusService).getBalance(NUMBER_CARD);
    }
}
