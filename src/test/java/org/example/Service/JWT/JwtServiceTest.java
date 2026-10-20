package org.example.Service.JWT;

import org.example.Config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Класс тестирования сервиса для работы с JWT-токенами
 */
class JwtServiceTest {
    /**
     * Время жизни токена
     */
    private static final long EXPIRATION_MS = 3_600_000L;
    /**
     * Секретный ключ
     */
    private static final String SECRET_KEY = "hAke9654tvQY8/C8tNFTBKPhqKkhwQ7y8iUbKTiDVEY=";
    /**
     * Объект тестируемого сервиса
     */
    private JwtService jwtService;
    /**
     * Константное тестовое значение логина
     */
    public static final String USERNAME = "testuser";
    /**
     * Константное тестовое значение - роль на чтение
     */
    public static final String ROLE_READ = "ROLE_READ";
    /**
     * Константное тестовое значение - роль на запись
     */
    private static final String ROLE_WRITE = "ROLE_WRITE";

    /**
     * Создание объекта сервиса перед каждым тестом
     */
    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties(SECRET_KEY, EXPIRATION_MS);
        jwtService = new JwtService(props);
    }

    /**
     * Тест - токен генерируется не пустым и соответствует общей структуре header.payload.signature
     */
    @Test
    void testTokenGenerateNotEmpty() {
        String token = jwtService.generateToken(USERNAME, ROLE_READ);
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(2, token.chars().filter(c -> c == '.').count());
        assertEquals(3, token.split("\\.").length);
    }

    /**
     * Тест - логин корректно извлекается из токена
     */
    @Test
    void testLoginCorrectlyExtractFromToken() {
        String token = jwtService.generateToken(USERNAME, ROLE_WRITE);
        String username = jwtService.extractUsername(token);
        assertEquals(USERNAME, username);
    }

    /**
     * Тест - роль корректно извлекается из токена
     */
    @Test
    void testRoleCorrectlyExtractFromToken() {
        String token = jwtService.generateToken(USERNAME, ROLE_WRITE);
        String roles = jwtService.extractRoles(token);
        assertEquals(ROLE_WRITE, roles);
    }

    /**
     * Тест - токен для разных пользователей не совпадает
     */
    @Test
    void testReturnsDifferentTokensForDifferentUsers() {
        String token1 = jwtService.generateToken("user1", ROLE_READ);
        String token2 = jwtService.generateToken("user2", ROLE_WRITE);
        assertNotEquals(token1, token2);
    }

    /**
     * Тест - для корректного токена успешно проходит проверка валидности
     */
    @Test
    void testGeneratedTokenPassValidityCheck() {
        String token = jwtService.generateToken(USERNAME, ROLE_READ);
        assertNotNull(token, "generateToken must produce a token");
        assertFalse(token.isEmpty(), "token must not be empty");
        assertTrue(jwtService.isValid(token));
    }

    /**
     * Тест - null-токен невалиден
     */
    @Test
    void testNullTokenIsInvalid() {
        assertFalse(jwtService.isValid(null));
    }

    /**
     * Тест - пустой токен невалиден
     */
    @Test
    void testEmptyTokenIsInvalid() {
        assertFalse(jwtService.isValid(""));
    }

    /**
     * Тест - некорректный токен невалидный
     */
    @Test
    void testIncorrectTokenIsInvalid() {
        assertFalse(jwtService.isValid("invalid.token.here"));
    }

    /**
     * Тест - выброс исключения при попытке извлечь логин из некорректного токена
     */
    @Test
    void testExceptionWhenExtractLoginFromInvalidToken() {
        assertThrows(Exception.class, () -> {
            jwtService.extractUsername("invalid.token");
        });
    }

    /**
     * Тест - выброс исключения при попытке извлечь роль из некорректного токена
     */
    @Test
    void testExceptionWhenExtractRoleFromInvalidToken() {
        assertThrows(Exception.class, () -> {
            jwtService.extractRoles("invalid.token");
        });
    }
}
