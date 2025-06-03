package ru.practicum.shareit.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class ErrorResponse {
    private String message;
}