package org.example.Controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.DTO.AuthResponse;
import org.example.DTO.LoginRequest;
import org.example.DTO.RegisterRequest;
import org.example.DTO.UserDTO;
import org.example.Enum.Role;
import org.example.Service.JwtService;
import org.example.Service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/bonus_server")
@AllArgsConstructor
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Регистрация пользователя
     * @param request запрос с логином, паролем и ролью
     * @return DTO с id и логином зарегистрированного пользователя
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO registerWrite(@Valid @RequestBody RegisterRequest request) {
        UserDTO response = userService.register(request);
        return response;
    }

    /**
     * Аутентификация пользователя
     * @param request запрос с логином и паролем
     * @return ответ с токеном
     */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword()));
        UserDetails user = (UserDetails) auth.getPrincipal();
        String token = jwtService.generateToken(user.getUsername(), user.getAuthorities().toString());
        return new AuthResponse(token);
    }
}
