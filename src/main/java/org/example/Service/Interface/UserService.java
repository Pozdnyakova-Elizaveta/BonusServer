package org.example.Service.Interface;

import org.example.DTO.Request.RegisterRequest;
import org.example.DTO.Response.UserResponse;

/**
 * Сервис управления пользователями
 */
public interface UserService {
    UserResponse register(RegisterRequest registerRequest);
}
