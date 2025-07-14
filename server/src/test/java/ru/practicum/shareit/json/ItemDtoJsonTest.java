package ru.practicum.shareit.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> json;

    @Test
    void serializeItemDto() throws Exception {
        ItemDto itemDto = new ItemDto(
                1L,
                "Дрель",
                "Простая дрель",
                true,
                1L);

        JsonContent<ItemDto> result = json.write(itemDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Дрель");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Простая дрель");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(1);
    }

    @Test
    void deserializeItemDto() throws Exception {
        String content = "{\n" +
                "  \"id\": 1,\n" +
                "  \"name\": \"Дрель\",\n" +
                "  \"description\": \"Простая дрель\",\n" +
                "  \"available\": true,\n" +
                "  \"ownerId\": 1\n" +
                "}";

        ItemDto result = json.parseObject(content);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Дрель");
        assertThat(result.getDescription()).isEqualTo("Простая дрель");
        assertThat(result.getAvailable()).isTrue();
    }
}