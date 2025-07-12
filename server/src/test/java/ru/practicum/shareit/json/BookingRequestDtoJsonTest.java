package ru.practicum.shareit.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingRequestDtoJsonTest {

    @Autowired
    private JacksonTester<BookingRequestDto> json;

    @Test
    void serializeBookingRequestDto() throws Exception {
        BookingRequestDto dto = new BookingRequestDto(
                LocalDateTime.of(2025, 7, 15, 12, 0),
                LocalDateTime.of(2025, 7, 16, 12, 0),
                42L
        );

        JsonContent<BookingRequestDto> result = json.write(dto);

        assertThat(result).extractingJsonPathStringValue("$.start")
                .isEqualTo("2025-07-15T12:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end")
                .isEqualTo("2025-07-16T12:00:00");
        assertThat(result).extractingJsonPathNumberValue("$.itemId")
                .isEqualTo(42);
    }

    @Test
    void deserializeBookingRequestDto() throws Exception {
        String content = "{\n" +
                "  \"start\": \"2025-07-15T12:00:00\",\n" +
                "  \"end\": \"2025-07-16T12:00:00\",\n" +
                "  \"itemId\": 42\n" +
                "}";


        BookingRequestDto result = json.parseObject(content);

        assertThat(result.getStart()).isEqualTo(LocalDateTime.of(2025, 7, 15, 12, 0));
        assertThat(result.getEnd()).isEqualTo(LocalDateTime.of(2025, 7, 16, 12, 0));
        assertThat(result.getItemId()).isEqualTo(42L);
    }
}
