package org.example.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Ответ на успешную аутентификацию
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    /**
     * JWT токен
     */
    private String token;
}