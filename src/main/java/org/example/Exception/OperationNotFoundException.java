package org.example.Exception;

import org.springframework.http.HttpStatus;

/**
 * Исключение - операция не найдена
 */
public class OperationNotFoundException extends ApplicationException {
    public OperationNotFoundException(Long id) {
        super("Operation not found: " + id);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
