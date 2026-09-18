package org.example.Service.Interface;

import org.example.DTO.Request.RegisterRequest;
import org.example.DTO.Response.UserResponce;

/**
 * Сервис управления пользователями
 */
public interface UserService {
    UserResponce register(RegisterRequest registerRequest);
}
