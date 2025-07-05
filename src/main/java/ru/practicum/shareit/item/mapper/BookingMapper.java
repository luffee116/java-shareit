package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.BookingDto;

public class BookingMapper {
    public static BookingDto toDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .build();

    }
}
