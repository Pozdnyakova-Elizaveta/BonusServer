package org.example.Controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.DTO.Response.AuthResponse;
import org.example.DTO.Request.LoginRequest;
import org.example.DTO.Request.RegisterRequest;
import org.example.DTO.Response.UserResponce;
import org.example.Service.JWT.JwtService;
import org.example.Service.Interface.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
    public UserResponce registerWrite(@Valid @RequestBody RegisterRequest request) {
        log.info("Register user: {}", request.getLogin());
        UserResponce response = userService.register(request);
        log.info("User is registered: {}, {}", response.getId(), response.getLogin());
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
        String role = user.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElseThrow(() -> new IllegalStateException("У пользователя нет ролей"));
        log.info("Login user {}", user.getUsername());
        String token = jwtService.generateToken(user.getUsername(), role);
        log.info("User {} is authenticated, token received", user.getUsername());
        return new AuthResponse(token);
    }
}
