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
 * Запрос на регистрацию
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class RegisterRequest {
    /**
     * Логин
     */
    @NotNull
    @NotBlank
    @Size(min = 8, max = 32)
    private String login;
    /**
     * Пароль
     */
    @NotNull
    @NotBlank
    @Size(min = 8, max = 32)
    private String password;
    /**
     * Роль для регистрации
     */
    @NotNull
    private Role role;
}
