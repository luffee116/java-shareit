package ru.practicum.shareit.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    private BookingRequestDto bookingRequestDto;
    private BookingResponseDto bookingResponseDto;

    @BeforeEach
    void setUp() {
        bookingRequestDto = BookingRequestDto.builder()
                .start(LocalDateTime.of(2025, 7, 20, 10, 0))
                .end(LocalDateTime.of(2025, 7, 21, 10, 0))
                .itemId(1L)
                .build();

        bookingResponseDto = BookingResponseDto.builder()
                .id(100L)
                .start(bookingRequestDto.getStart())
                .end(bookingRequestDto.getEnd())
                .status(ru.practicum.shareit.booking.model.BookingStatus.WAITING)
                .booker(UserDto.builder()
                        .id(1L)
                        .name("User")
                        .email("user@example.com")
                        .build())
                .item(ItemDto.builder()
                        .id(1L)
                        .name("Дрель")
                        .description("Простая дрель")
                        .available(true)
                        .requestId(null)
                        .build())
                .build();
    }

    @Test
    void createBookingTest() throws Exception {
        when(bookingService.createBooking(ArgumentMatchers.any(), ArgumentMatchers.anyLong()))
                .thenReturn(bookingResponseDto);

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", 1)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.status").value("WAITING"));

        verify(bookingService).createBooking(ArgumentMatchers.any(), ArgumentMatchers.eq(1L));
    }

    @Test
    void approveBookingTest() throws Exception {
        when(bookingService.approveBooking(100L, 1L, true))
                .thenReturn(bookingResponseDto);

        mockMvc.perform(patch("/bookings/100")
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L))
                .andExpect(jsonPath("$.status").value("WAITING"));

        verify(bookingService).approveBooking(100L, 1L, true);
    }

    @Test
    void getBookingTest() throws Exception {
        when(bookingService.getBookingById(100L, 1L))
                .thenReturn(bookingResponseDto);

        mockMvc.perform(get("/bookings/100")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));

        verify(bookingService).getBookingById(100L, 1L);
    }

    @Test
    void getUserBookingsTest() throws Exception {
        when(bookingService.getUserBookings(1L, BookingState.ALL, 0, 10))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L));

        verify(bookingService).getUserBookings(1L, BookingState.ALL, 0, 10);
    }

    @Test
    void getOwnerBookingsTest() throws Exception {
        when(bookingService.getOwnerBookings(1L, BookingState.ALL, 0, 10))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100L));

        verify(bookingService).getOwnerBookings(1L, BookingState.ALL, 0, 10);
    }
}
