package org.example.Service.JWT;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.example.Config.JwtProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Сервис для работы с JWT-токенами
 */
@Service
@AllArgsConstructor
public class JwtService {
    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final JwtProperties props;

    /**
     * Создание нового токена
     * @param username логин
     * @param roles роль
     * @return строка-токен
     */
    public String generateToken(String username, String roles) {
        var now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(props.getExpirationMs())))
                .signWith(key(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Парсинг токена для получения логина
     * @param token строка-токен
     * @return строка-логин
     */
    public String extractUsername(String token) {
        return parse(token).getSubject();
    }

    /**
     * Парсинг токена для получения роли
     * @param token строка-токен
     * @return строка-роль
     */
    public String extractRoles(String token) {
        return parse(token).get("roles").toString();
    }

    /**
     * Проверка валидности токена
     * @param token строка-токен
     * @return boolean-значение валидности
     */
    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("Invalid JWT: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Метод валидации - проверка подписи и срока действия
     * @param token строка-токен
     * @return объект Claims с данными ьокена
     */
    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Построение SecretKey на основе ключа из application.properties для проверки подписи
     * @return объект SecretKey
     */
    private SecretKey key() {
        return Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
