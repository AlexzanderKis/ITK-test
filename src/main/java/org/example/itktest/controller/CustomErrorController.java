package org.example.itktest.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.example.itktest.exception.ErrorResponseDTO;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/*
    перехватывает всё, что не попало в контроллер
 */
@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<ErrorResponseDTO> handleError(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        HttpStatus httpStatus = statusCode != null ? HttpStatus.valueOf(statusCode) : HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorResponseDTO body = new ErrorResponseDTO(
                LocalDateTime.now(),
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                "Ошибка: " + httpStatus.getReasonPhrase()
        );
        return new ResponseEntity<>(body, httpStatus);
    }
}
