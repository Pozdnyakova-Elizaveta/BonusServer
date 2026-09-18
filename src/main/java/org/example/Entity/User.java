package org.example.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;
import lombok.Builder;
import org.example.Enum.Role;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    /**
     * Идентификатор пользователя
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * Логин
     */
    @Column(nullable = false, unique = true)
    private String login;
    /**
     * Пароль
     */
    @Column(nullable = false, length = 60)
    private String password;
    /**
     * Роль - чтение или запись
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
