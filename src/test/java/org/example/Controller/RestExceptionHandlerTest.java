package org.example.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.DTO.Request.BonusOperationRequest;
import org.example.DTO.Request.RegisterRequest;
import org.example.Enum.Role;
import org.example.Service.JWT.JwtService;
import org.example.Service.Interface.BonusService;
import org.example.Service.Interface.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Класс тестирования обработки исключений
 */
@ExtendWith(MockitoExtension.class)
class RestExceptionHandlerTest {
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
     * Мок-объект сервиса пользователей
     */
    @Mock
    private UserService userService;
    /**
     * Мок-объект JWT-сервиса
     */
    @Mock
    private JwtService jwtService;
    /**
     * Маппер
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Создание тестовых объектов перед каждым тестом
     */
    @BeforeEach
    void setUp() {
        BonusController bonusController = new BonusController(bonusService);
        UserController userController = new UserController(userService, jwtService, null);
        mockMvc = MockMvcBuilders.standaloneSetup(bonusController, userController)
                .setControllerAdvice(new RestExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    //accrual

    /**
     * Тест - ответ 400 при невалидных полях для запроса на начисление баллов
     */
    @Test
    void testAccrualReturn400ForInvalidAccrualRequest() throws Exception {
        //некорректный номер карты, отрицательная сумма
        String invalidJson = "{\"cardNumber\":\"invalid\",\"amountBonus\":-10}";
        mockMvc.perform(post("/bonus-server/accrual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    /**
     * Тест - ответ 400 при отправке запроса с отсутствующими полями для начисления
     */
    @Test
    void testAccrualReturn400ForMissingFields() throws Exception {
        mockMvc.perform(post("/bonus-server/accrual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    //deduction

    /**
     * Тест - ответ 400 при невалидном номере карты для запроса на списание баллов
     */
    @Test
    void testDeductionReturnBadRequestForInvalidCardNumber() throws Exception {
        BonusOperationRequest invalidRequest = new BonusOperationRequest("badcard", new BigDecimal("50.00"));
        mockMvc.perform(post("/bonus-server/deduction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        verify(bonusService, never()).deduction(any());
    }

    /**
     * Тест - ответ 400 при отрицательной сумме для запроса на списание баллов
     */
    @Test
    void testDeductionReturnBadRequestForNegativeAmount() throws Exception {
        BonusOperationRequest invalidRequest = new BonusOperationRequest("1234567890123456", new BigDecimal("-50.00"));
        mockMvc.perform(post("/bonus-server/deduction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        verify(bonusService, never()).deduction(any());
    }

    /**
     * Тест - ответ 400 при отправке запроса с отсутствующими полями для списания баллов
     */
    @Test
    void testDeductionReturnBadRequestForMissingFieldsFor() throws Exception {
        mockMvc.perform(post("/bonus-server/deduction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        verify(bonusService, never()).deduction(any());
    }

    //cancel

    /**
     * Тест - ответ 400 при отправке запроса с отрицательным id для отмены операции
     */
    @Test
    void testCancelReturn400ForNegativeOperationId() throws Exception {
        mockMvc.perform(post("/bonus-server/operations/-1/cancel"))
                .andExpect(status().isBadRequest());
        verify(bonusService, never()).cancel(any());
    }

    /**
     * Тест - ответ 400 при отправке запроса с id = 0 для отмены операции
     */
    @Test
    void testCancelReturn400ForZeroOperationId() throws Exception {
        mockMvc.perform(post("/bonus-server/operations/0/cancel"))
                .andExpect(status().isBadRequest());
        verify(bonusService, never()).cancel(any());
    }

    /**
     * Тест - ответ 400 при отправке запроса с нечисловым id для отмены операции
     */
    @Test
    void testCancelReturn400ForNonNumericOperationId() throws Exception {
        mockMvc.perform(post("/bonus-server/operations/abc/cancel"))
                .andExpect(status().isBadRequest());
        verify(bonusService, never()).cancel(any());
    }

    //history

    /**
     * Тест - ответ 400 при отправке запроса с некорректным номером карты для получения истории
     */
    @Test
    void testHistoryReturn400ForInvalidCardNumber() throws Exception {
        mockMvc.perform(get("/bonus-server/history")
                        .param("cardNumber", "invalid"))
                .andExpect(status().isBadRequest());
        verify(bonusService, never()).getHistory(any(), any());
    }

    /**
     * Тест - ответ 400 при отправке запроса без номера карты для получения истории
     */
    @Test
    void testHistoryReturnBadRequestForMissingCardNumber() throws Exception {
        mockMvc.perform(get("/bonus-server/history"))
                .andExpect(status().isBadRequest());
        verify(bonusService, never()).getHistory(any(), any());
    }

    //balance

    /**
     * Тест - ответ 400 при отправке запроса с некорректным номером карты для получения баланса
     */
    @Test
    void testBalanceReturn400ForInvalidCardNumber() throws Exception {
        mockMvc.perform(get("/bonus-server/balance")
                        .param("cardNumber", "invalid"))
                .andExpect(status().isBadRequest());
        verify(bonusService, never()).getBalance(any());
    }

    /**
     * Тест - ответ 400 при отправке запроса без номера карты для получения баланса
     */
    @Test
    void testBalanceReturn400ForMissingCardNumber() throws Exception {
        mockMvc.perform(get("/bonus-server/balance"))
                .andExpect(status().isBadRequest());
        verify(bonusService, never()).getBalance(any());
    }

    //register

    /**
     * Тест - ответ 400 при регистрации с невалидным логином/паролем
     */
    @Test
    void testRegisterReturnBadRequestForInvalidRequest() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest("", "", null);
        mockMvc.perform(post("/bonus-server/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        verify(userService, never()).register(any());
    }

    /**
     * Тест - ответ 400 при регистрации со слишком коротким логином
     */
    @Test
    void testRegisterReturnBadRequestForTooShortLogin() throws Exception {
        RegisterRequest invalidRequest = new RegisterRequest("short", "123", Role.ROLE_READ);
        mockMvc.perform(post("/bonus-server/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
        verify(userService, never()).register(any());
    }

    /**
     * Тест - ответ 400 при регистрации без необходимых переданных параметов
     */
    @Test
    void testRegisterReturnBadRequestForMissingFields() throws Exception {
        // when & then
        mockMvc.perform(post("/bonus-server/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).register(any());
    }

    //login

    /**
     * Тест - ответ 400 при аутентификации без необходимых переданных параметов
     */
    @Test
    void testLoginReturnBadRequestForMissingFields() throws Exception {
        // when & then
        mockMvc.perform(post("/bonus-server/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
