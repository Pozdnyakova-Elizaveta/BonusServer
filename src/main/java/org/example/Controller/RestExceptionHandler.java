package org.example.Controller;

import org.example.Exception.ApplicationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Обработчик исключений REST-контроллеров
 */
@RestControllerAdvice
public class RestExceptionHandler {
    /**
     * Обработка бизнес-исключений приложения
     *
     * @param e выброшенное исключение
     * @return ответ со статусом 400 и сообщение об ошибке
     */
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<String> handleApplication(ApplicationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    /**
     * Обработка ошибок валидации
     *
     * @param e выброшенное исключение
     * @return ответ со статусом 400 и перечисление ошибок валидации
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(msg);
    }
}
