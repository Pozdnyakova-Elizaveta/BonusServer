package org.example.Exception;

/**
 * Базовое исключение для бизнес-ошибок
 */
public class ApplicationException extends RuntimeException {
    public ApplicationException(String message) {
        super(message);
    }
}