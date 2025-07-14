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
import ru.practicum.shareit.itemrequest.ItemRequestClient;
import ru.practicum.shareit.itemrequest.dto.ItemRequestCreateDto;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ShareItGateway.class)
@AutoConfigureMockMvc
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ItemRequestClient itemRequestClient;

    private ItemRequestCreateDto requestCreateDto;

    @BeforeEach
    void setUp() {
        requestCreateDto = new ItemRequestCreateDto();
        requestCreateDto.setDescription("Need item for my project");
    }

    @Test
    void createRequest_WhenValidRequest_ThenReturnOk() throws Exception {
        Mockito.when(itemRequestClient.createRequest(anyLong(), any()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(requestCreateDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk());
    }

    @Test
    void createRequest_WhenMissingUserIdHeader_ThenReturnBadRequest() throws Exception {
        mockMvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(requestCreateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRequest_WhenEmptyDescription_ThenReturnBadRequest() throws Exception {
        requestCreateDto.setDescription("");

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .content(mapper.writeValueAsString(requestCreateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserRequests_WhenValidRequest_ThenReturnOk() throws Exception {
        Mockito.when(itemRequestClient.getUserRequests(anyLong()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getUserRequests_WhenMissingUserIdHeader_ThenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllRequests_WhenValidRequest_ThenReturnOk() throws Exception {
        Mockito.when(itemRequestClient.getAllRequests(anyLong(), anyInt(), anyInt()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getRequestById_WhenValidRequest_ThenReturnOk() throws Exception {
        Mockito.when(itemRequestClient.getRequestById(anyLong(), anyLong()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getRequestById_WhenMissingUserIdHeader_ThenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests/1"))
                .andExpect(status().isBadRequest());
    }
}