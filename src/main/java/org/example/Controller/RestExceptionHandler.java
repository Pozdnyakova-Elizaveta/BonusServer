package org.example.Controller;

import org.example.Exception.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.stream.Collectors;

/**
 * Обработчик исключений REST-контроллеров
 */
@RestControllerAdvice
public class RestExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    /**
     * Обработка бизнес-исключений приложения
     *
     * @param e выброшенное исключение
     * @return ответ со статусом 400 и сообщение об ошибке
     */
    @ExceptionHandler(ApplicationException.class)
    public ProblemDetail handleApplication(ApplicationException e) {
        log.warn("Application error: {}", e.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problemDetail.setTitle("Application error");
        return problemDetail;
    }

    /**
     * Обработка ошибок валидации
     *
     * @param e выброшенное исключение
     * @return ответ со статусом 400 и перечисление ошибок валидации
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException e) {
        log.warn("Validation error: {}", e.getMessage());
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, msg);
        problemDetail.setTitle("Validation error");
        return problemDetail;
    }

    /**
     * Обработка ошибок валидации параметров метода
     *
     * @param e выброшенное исключение
     * @return ответ со статусом 400 и перечисление ошибок валидации
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ProblemDetail handleMethodValidation(HandlerMethodValidationException e) {
        log.warn("Method validation error: {}", e.getMessage());
        String msg = e.getParameterValidationResults().stream()
                .flatMap(result -> {
                    String paramName = result.getMethodParameter().getParameterName();
                    return result.getResolvableErrors().stream()
                            .map(MessageSourceResolvable::getDefaultMessage)
                            .map(error -> paramName + ": " + error);
                })
                .collect(Collectors.joining("; "));
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, msg);
        problemDetail.setTitle("Validation error");
        return problemDetail;
    }

    /**
     * Обработка исключений оптимистической блокировка
     *
     * @param e выброшенное исключение
     * @return ответ со статусом 409 и сообщение об ошибке
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLockFailure(ObjectOptimisticLockingFailureException e) {
        log.warn("Optimistic lock exception: {}", e.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT, "Record was modified by another request");
        problemDetail.setTitle("Optimistic lock exception");
        return problemDetail;
    }

    /**
     * Обработка всех остальных исключений
     *
     * @param e выброшенное исключение
     * @return ответ со статусом 500 и сообщение об ошибке
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAll(Exception e) {
        log.error("Unhandled exception", e);
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
        problemDetail.setTitle("Internal server error");
        return problemDetail;
    }
}
