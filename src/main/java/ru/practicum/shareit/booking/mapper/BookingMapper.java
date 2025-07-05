package ru.practicum.shareit.booking.mapper;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.user.mapper.UserMapper;

public class BookingMapper {

    public static BookingResponseDto toDto(final Booking booking) {
        return BookingResponseDto.builder()
                .id(booking.getId())
                .end(booking.getEnd())
                .start(booking.getStart())
                .booker(UserMapper.toUserDto(booking.getBooker()))
                .item(ItemMapper.toItemDto(booking.getItem()))
                .status(booking.getStatus())
                .build();
    }

    public static Booking toBooking(final BookingRequestDto bookingRequestDto) {
        return Booking.builder()
                .start(bookingRequestDto.getStart())
                .end(bookingRequestDto.getEnd())
                .build();

    }
}
