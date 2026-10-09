package org.example.Exception;

import org.springframework.http.HttpStatus;

/**
 * Исключение - пользователь с данным логином уже зарегистрирован
 */
public class LoginAlreadyExistsException extends ApplicationException{
    public LoginAlreadyExistsException(String login) {
        super(String.format("Authorization error for login: %s", login));
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }
}
