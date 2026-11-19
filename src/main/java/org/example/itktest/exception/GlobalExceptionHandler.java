package org.example.itktest.exception;

import jakarta.persistence.QueryTimeoutException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/*
    перехватывает бизнес-исключения, ошибки валидации полей, некорректные JSON,
    возвращает HTTP-статусы
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // статусы ошибок строкой
    private static final String NOT_FOUND = "?NOT_FOUND?";
    private static final String BAD_REQUEST = "?BAD_REQUEST?";
    private static final String INTERNAL_SERVER_ERROR = "?INTERNAL_SERVER_ERROR?";
    private static final String CONFLICT = "?CONFLICT?";
    private static final String SERVICE_UNAVAILABLE = "?SERVICE_UNAVAILABLE?";

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleWalletNotFoundExc(WalletNotFoundException e){
        return buildResponse(HttpStatus.NOT_FOUND,
                NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(NotEnoughFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotEnoughFoundsExc(NotEnoughFoundException e){

        return buildResponse(HttpStatus.CONFLICT,
                CONFLICT, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodValidationExc(MethodArgumentNotValidException e){

        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() +": "+fieldError.getDefaultMessage())
                .collect(Collectors.joining(" | "));

        return buildResponse(HttpStatus.BAD_REQUEST, "Ошибка валидации: ", msg);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotReadableExc(HttpMessageNotReadableException e){

        return buildResponse(HttpStatus.BAD_REQUEST,
                BAD_REQUEST,"Неправильный JSON или формат");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleArgumentTypeMismatchExc(MethodArgumentTypeMismatchException e){

        return buildResponse(HttpStatus.BAD_REQUEST,
                BAD_REQUEST,"Неправильный параметр метода: "+e.getName());
    }

    // обработчики при нагрузке
    @ExceptionHandler({
            PessimisticLockingFailureException.class,
            CannotAcquireLockException.class,
            QueryTimeoutException.class
    })
    public ResponseEntity<ErrorResponseDTO> handleTransient(Exception e) {
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE,
                "Ресурс занят, повторите запрос");
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponseDTO> handleOptimistic(Exception e) {
        return buildResponse(HttpStatus.CONFLICT, CONFLICT,
                "Параллельная модификация, повторите запрос");
    }

    @ExceptionHandler({
            CannotGetJdbcConnectionException.class,
            CannotCreateTransactionException.class
    })
    public ResponseEntity<ErrorResponseDTO> handlePoolExhaustion(Exception e) {
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, SERVICE_UNAVAILABLE,
                "Сервер перегружен, повторите запрос");
    }

    // другие ошибки
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleUnexpectedExceptionExc(Exception e){

        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                INTERNAL_SERVER_ERROR,
                "Непредвиденная ошибка");
    }

    // DRY
    private ResponseEntity<ErrorResponseDTO> buildResponse(HttpStatus status, String error, String msg){
        ErrorResponseDTO errorBody = new ErrorResponseDTO(
                LocalDateTime.now(),
                status.value(),
                error,
                msg);

        return new ResponseEntity<>(errorBody, status);
    }
}
