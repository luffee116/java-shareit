package ru.practicum.shareit.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void serializeItemRequestDto() throws Exception {
        LocalDateTime created = LocalDateTime.of(2025, 7, 11, 10, 30);

        ItemDto item = ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .requestId(2L)
                .build();

        ItemRequestDto dto = ItemRequestDto.builder()
                .id(10L)
                .description("Нужна дрель")
                .created(created)
                .requestorId(5L)
                .items(List.of(item))
                .build();

        JsonContent<ItemRequestDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(10);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Нужна дрель");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2025-07-11T10:30:00");
        assertThat(result).extractingJsonPathNumberValue("$.requestorId").isEqualTo(5);
        assertThat(result).extractingJsonPathArrayValue("$.items").hasSize(1);
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Дрель");
    }

    @Test
    void deserializeItemRequestDto() throws Exception {
        String content = "{\n" +
                "  \"id\": 10,\n" +
                "  \"description\": \"Нужна дрель\",\n" +
                "  \"created\": \"2025-07-11T10:30:00\",\n" +
                "  \"requestorId\": 5,\n" +
                "  \"items\": [\n" +
                "    {\n" +
                "      \"id\": 1,\n" +
                "      \"name\": \"Дрель\",\n" +
                "      \"description\": \"Простая дрель\",\n" +
                "      \"available\": true,\n" +
                "      \"requestId\": 2\n" +
                "    }\n" +
                "  ]\n" +
                "}";


        ItemRequestDto dto = json.parseObject(content);

        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getDescription()).isEqualTo("Нужна дрель");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2025, 7, 11, 10, 30));
        assertThat(dto.getRequestorId()).isEqualTo(5L);
        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().get(0).getName()).isEqualTo("Дрель");
    }
}
