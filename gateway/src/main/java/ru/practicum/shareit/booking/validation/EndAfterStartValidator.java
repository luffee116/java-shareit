package ru.practicum.shareit.booking.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;

public class EndAfterStartValidator implements ConstraintValidator<EndAfterStart, BookItemRequestDto> {

    @Override
    public void initialize(EndAfterStart constraintAnnotation) {
    }

    @Override
    public boolean isValid(BookItemRequestDto value, ConstraintValidatorContext context) {
        if (value.getStart() == null || value.getEnd() == null) {
            return true; // Пусть другие аннотации обрабатывают null
        }
        return value.getEnd().isAfter(value.getStart());
    }
}