package org.example.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.Enum.Role;

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
