package ru.practicum.shareit.unit;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.BookingDto;
import ru.practicum.shareit.item.mapper.BookingMapper;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.*;

class BookingMapperTest {

    @Test
    void toDto_shouldMapBookingToBookingDto() {
        User booker = new User();
        booker.setId(42L);

        Booking booking = new Booking();
        booking.setId(100L);
        booking.setBooker(booker);

        BookingDto dto = BookingMapper.toDto(booking);

        assertNotNull(dto);
        assertEquals(100L, dto.getId());
        assertEquals(42L, dto.getBookerId());
    }
}
