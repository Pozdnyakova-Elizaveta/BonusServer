package org.example.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Запрос на аутентификацию
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    /**
     * Логин
     */
    @Size(max = 128)
    @NotBlank
    private String login;
    /**
     * Пароль
     */
    @Size(max = 128)
    @NotBlank
    private String password;
}
