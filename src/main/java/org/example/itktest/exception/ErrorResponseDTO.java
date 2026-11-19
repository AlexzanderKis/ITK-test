package org.example.itktest.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
        DTO для ошибок
        Чтобы ошибки возвращались в JSON-формате
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponseDTO {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
    }
