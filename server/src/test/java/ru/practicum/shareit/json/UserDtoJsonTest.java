package ru.practicum.shareit.json;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class UserDtoJsonTest {

    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    void serializeUserDto() throws Exception {
        UserDto userDto = new UserDto(
                1L,
                "Иван Иванов",
                "ivan@example.com"
        );

        JsonContent<UserDto> result = json.write(userDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Иван Иванов");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("ivan@example.com");
    }

    @Test
    void deserializeUserDto() throws Exception {
        String content = "{\n" +
                "  \"id\": 1,\n" +
                "  \"name\": \"Иван Иванов\",\n" +
                "  \"email\": \"ivan@example.com\"\n" +
                "}";


        UserDto result = json.parseObject(content);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Иван Иванов");
        assertThat(result.getEmail()).isEqualTo("ivan@example.com");
    }

}
