package org.example.Service.Impl;

import org.example.DTO.Request.RegisterRequest;
import org.example.DTO.Response.UserResponse;
import org.example.Entity.User;
import org.example.Enum.Role;
import org.example.Exception.LoginAlreadyExistsException;
import org.example.Repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Класс тестирования сервиса пользователей
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    /**
     * Мок-объект репозитория пользователей
     */
    @Mock
    private UserRepository userRepository;
    /**
     * Мок-объект кодирования паролей
     */
    @Mock
    private PasswordEncoder passwordEncoder;
    /**
     * Объект тестируемого сервиса
     */
    @InjectMocks
    private UserServiceImpl userService;

    /**
     * Тестовые константы
     */
    private static final String FIRST_LOGIN = "testuser";
    private static final String PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "encodedPassword";
    /**
     * Запрос на регистрацию
     */
    private final RegisterRequest registerRequest = new RegisterRequest(FIRST_LOGIN, PASSWORD, Role.ROLE_READ);
    /**
     * Сущность пользователя
     */
    private final User user = User.builder()
            .id(1L)
            .login(FIRST_LOGIN)
            .password(ENCODED_PASSWORD)
            .role(Role.ROLE_READ)
            .build();

    /**
     * Тест - успешная регистрация пользователя
     */
    @Test
    void testSuccessfulRegistration() {
        when(userRepository.findByLogin(FIRST_LOGIN)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(user);
        UserResponse response = userService.register(registerRequest);
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(FIRST_LOGIN, response.getLogin());
        verify(userRepository).findByLogin(FIRST_LOGIN);
        verify(passwordEncoder).encode(PASSWORD);
        verify(userRepository, times(1)).save(any(User.class));
        verify(userRepository).save(argThat(savedUser ->
                savedUser.getRole() == Role.ROLE_READ
        ));
    }

    /**
     * Тест - исключение при попытке регистрации с логином, который уже используется
     */
    @Test
    void testRegistrationExceptionWhenLoginAlreadyExists() {
        User existingUser = User.builder()
                .id(2L)
                .login(FIRST_LOGIN)
                .password("existingPassword")
                .role(Role.ROLE_WRITE)
                .build();
        when(userRepository.findByLogin(FIRST_LOGIN)).thenReturn(Optional.of(existingUser));
        LoginAlreadyExistsException exception = assertThrows(LoginAlreadyExistsException.class, () -> {
            userService.register(registerRequest);
        });
        assertTrue(exception.getMessage().contains(FIRST_LOGIN));
        verify(userRepository).findByLogin(FIRST_LOGIN);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    /**
     * Тест - пароль хешируется перед сохранением в БД
     */
    @Test
    void testEncodePasswordBeforeSaving() {
        when(userRepository.findByLogin(FIRST_LOGIN)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(user);
        userService.register(registerRequest);
        verify(passwordEncoder).encode(PASSWORD);
        verify(userRepository).save(argThat(savedUser ->
                savedUser.getPassword().equals(ENCODED_PASSWORD)
        ));
    }
}
