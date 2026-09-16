package org.example.Exception;

/**
 * Исключение - операция не найдена
 */
public class OperationNotFoundException extends ApplicationException {
    public OperationNotFoundException(Long id) {
        super("Operation not found: " + id);
    }
}
