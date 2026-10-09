package org.example.Exception;

import org.springframework.http.HttpStatus;

/**
 * Исключение - попытка отмены уже отмененной операции
 */
public class OperationAlreadyCanceledException extends ApplicationException {
    public OperationAlreadyCanceledException(Long id) {
        super(String.format("Operation with id %d is already canceled", id));
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.CONFLICT;
    }
}
