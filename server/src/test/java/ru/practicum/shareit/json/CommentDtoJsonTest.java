package ru.practicum.shareit.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.CommentDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentDtoJsonTest {

    @Autowired
    private JacksonTester<CommentDto> json;

    @Test
    void serializeCommentDto() throws Exception {
        LocalDateTime created = LocalDateTime.of(2025, 7, 11, 12, 0);

        CommentDto dto = CommentDto.builder()
                .id(1L)
                .text("Очень полезная вещь")
                .authorName("Иван Иванов")
                .created(created)
                .build();

        JsonContent<CommentDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("Очень полезная вещь");
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("Иван Иванов");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2025-07-11T12:00:00");
    }

    @Test
    void deserializeCommentDto() throws Exception {
        String content = "{\n" +
                "  \"id\": 1,\n" +
                "  \"text\": \"Очень полезная вещь\",\n" +
                "  \"authorName\": \"Иван Иванов\",\n" +
                "  \"created\": \"2025-07-11T12:00:00\"\n" +
                "}";


        CommentDto dto = json.parseObject(content);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getText()).isEqualTo("Очень полезная вещь");
        assertThat(dto.getAuthorName()).isEqualTo("Иван Иванов");
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2025, 7, 11, 12, 0));
    }
}
