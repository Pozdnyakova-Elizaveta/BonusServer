package org.example.Exception;

/**
 * Исключение - пользователь с данным логином уже зарегистрирован
 */
public class LoginAlreadyExistsException extends RuntimeException{
    public LoginAlreadyExistsException(String message) {
        super(message);
    }
}
