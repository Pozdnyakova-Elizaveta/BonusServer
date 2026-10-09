package org.example.Exception;

import org.springframework.http.HttpStatus;

/**
 * Исключение - попытка отмены для операции отмены
 */
public class UnacceptableCancelException extends ApplicationException {
    public UnacceptableCancelException(Long id) {
        super("Attempt to cancel the cancellation operation: " + id);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.UNPROCESSABLE_CONTENT;
    }
}
