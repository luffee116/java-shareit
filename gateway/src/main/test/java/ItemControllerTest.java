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
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ShareItGateway.class)
@AutoConfigureMockMvc
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemClient itemClient;

    private ItemRequestDto itemRequestDto;
    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        itemRequestDto = new ItemRequestDto();
        itemRequestDto.setName("Item name");
        itemRequestDto.setDescription("Item description");
        itemRequestDto.setAvailable(true);

        commentDto = new CommentDto();
        commentDto.setText("Test comment");
    }

    @Test
    void create_WhenValidRequest_ThenReturnOk() throws Exception {
        Mockito.when(itemClient.create(anyLong(), any()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk());
    }

    @Test
    void create_WhenMissingUserIdHeader_ThenReturnBadRequest() throws Exception {
        mockMvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_WhenInvalidItem_ThenReturnBadRequest() throws Exception {
        itemRequestDto.setName("");

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_WhenValidRequest_ThenReturnOk() throws Exception {
        Mockito.when(itemClient.update(anyLong(), anyLong(), any()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void update_WhenMissingUserIdHeader_ThenReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/items/1")
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_WhenValidRequest_ThenReturnOk() throws Exception {
        Mockito.when(itemClient.getById(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getAllByUser_WhenValidRequest_ThenReturnOk() throws Exception {
        Mockito.when(itemClient.getAllByUser(anyLong()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void search_WhenValidRequest_ThenReturnOk() throws Exception {
        Mockito.when(itemClient.search(anyString()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/items/search")
                        .param("text", "test"))
                .andExpect(status().isOk());
    }

    @Test
    void search_WhenEmptyText_ThenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_WhenValidRequest_ThenReturnOk() throws Exception {
        Mockito.when(itemClient.delete(anyLong()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(delete("/items/1"))
                .andExpect(status().isOk());
    }

    @Test
    void createComment_WhenValidRequest_ThenReturnOk() throws Exception {
        Mockito.when(itemClient.createComment(anyLong(), anyLong(), any()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(commentDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void createComment_WhenEmptyText_ThenReturnBadRequest() throws Exception {
        commentDto.setText("");

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(commentDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_WhenMissingUserIdHeader_ThenReturnBadRequest() throws Exception {
        mockMvc.perform(post("/items/1/comment")
                        .content(mapper.writeValueAsString(commentDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}