package org.example.Exception;

/**
 * Исключение - попытка отмены для операции отмены
 */
public class UnacceptableCancelException extends ApplicationException {
    public UnacceptableCancelException(Long id) {
        super("Attempt to cancel the cancellation operation: " + id);
    }
}
