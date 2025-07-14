import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.ShareItGateway;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ShareItGateway.class)
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserClient userClient;

    private UserRequestDto userRequestDto;
    private UserUpdateDto userUpdateDto;

    @BeforeEach
    void setUp() {
        userRequestDto = new UserRequestDto();
        userRequestDto.setName("User Name");
        userRequestDto.setEmail("user@example.com");

        userUpdateDto = new UserUpdateDto();
        userUpdateDto.setName("Updated Name");
        userUpdateDto.setEmail("updated@example.com");
    }

    @Test
    void create_WhenValidRequest_ThenReturnOk() throws Exception {
        Mockito.when(userClient.create(any()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userRequestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk());
    }

    @Test
    void create_WhenInvalidEmail_ThenReturnBadRequest() throws Exception {
        userRequestDto.setEmail("invalid-email");

        mockMvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userRequestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_WhenEmptyName_ThenReturnBadRequest() throws Exception {
        userRequestDto.setName("");

        mockMvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userRequestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_WhenValidRequest_ThenReturnOk() throws Exception {
        Mockito.when(userClient.update(anyLong(), any()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(userUpdateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void update_WhenInvalidEmail_ThenReturnBadRequest() throws Exception {
        userUpdateDto.setEmail("invalid-email");

        mockMvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(userUpdateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_WhenValidRequest_ThenReturnOk() throws Exception {
        Mockito.when(userClient.getById(anyLong()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_WhenValidRequest_ThenReturnOk() throws Exception {
        Mockito.when(userClient.getAll())
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());
    }

    @Test
    void delete_WhenValidRequest_ThenReturnOk() throws Exception {
        Mockito.when(userClient.delete(anyLong()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    void update_WhenPartialUpdate_ThenReturnOk() throws Exception {
        UserUpdateDto partialUpdate = new UserUpdateDto();
        partialUpdate.setEmail("partial@example.com");

        Mockito.when(userClient.update(anyLong(), any()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/users/1")
                        .content(mapper.writeValueAsString(partialUpdate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void create_WhenMissingBody_ThenReturnBadRequest() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
