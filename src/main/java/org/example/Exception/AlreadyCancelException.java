package org.example.Exception;

/**
 * Исключение - попытка отмены уже отмененной операции
 */
public class AlreadyCancelException extends ApplicationException {
    public AlreadyCancelException(Long id) {
        super("Operation already cancel: " + id);
    }
}
