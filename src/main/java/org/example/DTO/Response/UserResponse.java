package org.example.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO для передачи данных о регистрации
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    /**
     * Идентификатор пользователя
     */
    private Long id;
    /**
     * Логин
     */
    private String login;
}
