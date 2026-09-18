package org.example.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Запрос на аутентификацию
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class LoginRequest {
    /**
     * Логин
     */
    @NotNull
    @NotBlank
    private String login;
    /**
     * Пароль
     */
    @NotNull
    @NotBlank
    private String password;
}
