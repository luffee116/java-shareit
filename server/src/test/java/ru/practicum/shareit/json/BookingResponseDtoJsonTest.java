package ru.practicum.shareit.json;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingResponseDtoJsonTest {

    @Autowired
    private JacksonTester<BookingResponseDto> json;

    @Test
    void serializeBookingResponseDto() throws Exception {
        BookingResponseDto dto = new BookingResponseDto(
                100L,
                LocalDateTime.of(2025, 7, 15, 12, 0),
                LocalDateTime.of(2025, 7, 16, 12, 0),
                BookingStatus.APPROVED,
                new UserDto(1L, "Иван", "ivan@example.com"),
                new ItemDto(2L, "Дрель", "Описание", true, 3L)
        );

        JsonContent<BookingResponseDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(100);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2025-07-15T12:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2025-07-16T12:00:00");
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("APPROVED");
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.booker.name").isEqualTo("Иван");
        assertThat(result).extractingJsonPathStringValue("$.booker.email").isEqualTo("ivan@example.com");
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("Дрель");
        assertThat(result).extractingJsonPathStringValue("$.item.description").isEqualTo("Описание");
        assertThat(result).extractingJsonPathBooleanValue("$.item.available").isTrue();
        assertThat(result).extractingJsonPathNumberValue("$.item.requestId").isEqualTo(3);
    }

    @Test
    void deserializeBookingResponseDto() throws Exception {
        String content = """
                {
                  "id": 100,
                  "start": "2025-07-15T12:00:00",
                  "end": "2025-07-16T12:00:00",
                  "status": "APPROVED",
                  "booker": {
                    "id": 1,
                    "name": "Иван",
                    "email": "ivan@example.com"
                  },
                  "item": {
                    "id": 2,
                    "name": "Дрель",
                    "description": "Описание",
                    "available": true,
                    "requestId": 3
                  }
                }
                """;

        BookingResponseDto result = json.parseObject(content);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.APPROVED);
        assertThat(result.getStart()).isEqualTo(LocalDateTime.of(2025, 7, 15, 12, 0));
        assertThat(result.getEnd()).isEqualTo(LocalDateTime.of(2025, 7, 16, 12, 0));
        assertThat(result.getBooker().getId()).isEqualTo(1L);
        assertThat(result.getBooker().getName()).isEqualTo("Иван");
        assertThat(result.getBooker().getEmail()).isEqualTo("ivan@example.com");
        assertThat(result.getItem().getId()).isEqualTo(2L);
        assertThat(result.getItem().getName()).isEqualTo("Дрель");
        assertThat(result.getItem().getDescription()).isEqualTo("Описание");
        assertThat(result.getItem().getAvailable()).isTrue();
        assertThat(result.getItem().getRequestId()).isEqualTo(3L);
    }
}
