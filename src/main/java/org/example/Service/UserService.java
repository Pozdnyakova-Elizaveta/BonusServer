package org.example.Service;

import org.example.DTO.RegisterRequest;
import org.example.DTO.UserDTO;

/**
 * Сервис управления пользователями
 */
public interface UserService {
    UserDTO register(RegisterRequest registerRequest);
}
