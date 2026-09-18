package org.example.Service.Impl;

import lombok.AllArgsConstructor;
import org.example.DTO.Request.RegisterRequest;
import org.example.DTO.Response.UserResponce;
import org.example.Entity.User;
import org.example.Exception.LoginAlreadyExistsException;
import org.example.Repository.UserRepository;
import org.example.Service.Interface.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    /**
     * Регистрация пользователя
     * @param registerRequest запрос на регистрацию
     * @return dto-объект для созданного пользователя
     */
    public UserResponce register(RegisterRequest registerRequest){
        String login = registerRequest.getLogin();
        if (userRepository.findByLogin(login).isPresent()) {
            throw new LoginAlreadyExistsException(login);
        }
        String hashPassword = passwordEncoder.encode(registerRequest.getPassword());
        User user = User.builder().login(login).password(hashPassword)
                .role(registerRequest.getRole()).build();
        User registerUser = userRepository.save(user);
        return toDTO(registerUser);

    }

    /**
     * Преобразование сущности пользователя в DTO-объект
     * @param user сущность пользователя
     * @return dto-объект пользователя
     */
    private UserResponce toDTO(User user){
        return UserResponce.builder().id(user.getId()).login(user.getLogin()).build();
    }
}
