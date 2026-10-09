package org.example.Exception;

import org.springframework.http.HttpStatus;

/**
 * Базовое исключение для бизнес-ошибок
 */
public class ApplicationException extends RuntimeException {
    public ApplicationException(String message) {
        super(message);
    }
    public HttpStatus getStatus(){
        return HttpStatus.BAD_REQUEST;
    }
}