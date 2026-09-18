package org.example.Config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Настройки jwt
 */
@Getter
@Setter
@AllArgsConstructor
public class JwtProperties {
    /**
     * Секретный ключ
     */
    private String secret;
    /**
     * Время жизни access-токена
     */
    private Long expirationMs;
}
