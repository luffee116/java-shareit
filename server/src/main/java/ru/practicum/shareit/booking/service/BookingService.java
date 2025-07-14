package ru.practicum.shareit.booking.service;


import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;


import java.util.List;

public interface BookingService {

    BookingResponseDto createBooking(BookingRequestDto bookingDto, Long userId);

    BookingResponseDto approveBooking(Long bookingId, Long ownerId, boolean approved);

    BookingResponseDto getBookingById(Long bookingId, Long userId);

    List<BookingResponseDto> getUserBookings(Long userId, BookingState state, int from, int size);

    List<BookingResponseDto> getOwnerBookings(Long ownerId, BookingState state, int from, int size);
}