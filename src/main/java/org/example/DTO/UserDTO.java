package org.example.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO для передачи данных о регистрации
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserDTO {
    /**
     * Идентификатор пользователя
     */
    private Long id;
    /**
     * Логин
     */
    private String login;
}
