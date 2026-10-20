package org.example.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.DTO.Request.LoginRequest;
import org.example.DTO.Request.RegisterRequest;
import org.example.DTO.Response.UserResponse;
import org.example.Enum.Role;
import org.example.Service.JWT.JwtService;
import org.example.Service.Interface.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collection;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Класс тестирования контроллера для пользователей
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    /**
     * Мок-объект для имитации http-запросов
     */
    private MockMvc mockMvc;
    /**
     * Мок-объект сервиса пользователей
     */
    @Mock
    private UserService userService;
    /**
     * Объект контроллера с сервисами-заглушками
     */
    @InjectMocks
    private UserController userController;
    /**
     * Маппер
     */
    private final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * Тестовые запросы
     */
    private RegisterRequest registerRequest;
    private UserResponse userResponse;

    /**
     * Тестовые данные
     */
    private static final String LOGIN = "testuser";
    private static final String PASSWORD = "password123";

    /**
     * Создание тестовых объектов перед каждым тестом
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new RestExceptionHandler())
                .build();

        registerRequest = new RegisterRequest(LOGIN, PASSWORD, Role.ROLE_READ);
        userResponse = UserResponse.builder().id(1L).login(LOGIN).build();
    }

    /**
     * Тест - регистрация нового пользователя
     */
    @Test
    void testRegisterReturnCreatedUser() throws Exception {
        when(userService.register(any(RegisterRequest.class))).thenReturn(userResponse);
        mockMvc.perform(post("/bonus-server/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.login").value(LOGIN));
        verify(userService).register(any(RegisterRequest.class));
    }
}
