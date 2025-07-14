package ru.practicum.shareit.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemRequestService itemRequestService;

    @Autowired
    private ObjectMapper objectMapper;

    private ItemRequestInputDto inputDto;
    private ItemRequestDto responseDto;

    @BeforeEach
    void setUp() {
        inputDto = ItemRequestInputDto.builder()
                .description("Нужен молоток")
                .build();

        responseDto = ItemRequestDto.builder()
                .id(1L)
                .description("Нужен молоток")
                .created(LocalDateTime.of(2025, 7, 20, 12, 0))
                .requestorId(1L)
                .items(List.of(
                        ItemDto.builder()
                                .id(10L)
                                .name("Молоток")
                                .description("Хороший молоток")
                                .available(true)
                                .requestId(1L)
                                .build()
                ))
                .build();
    }

    @Test
    void createRequestTest() throws Exception {
        when(itemRequestService.createRequest(eq(1L), any(ItemRequestInputDto.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Нужен молоток"));

        verify(itemRequestService).createRequest(eq(1L), any(ItemRequestInputDto.class));
    }

    @Test
    void getUserRequestsTest() throws Exception {
        when(itemRequestService.getUserRequests(1L))
                .thenReturn(List.of(responseDto));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].description").value("Нужен молоток"));

        verify(itemRequestService).getUserRequests(1L);
    }

    @Test
    void getAllRequestsTest() throws Exception {
        when(itemRequestService.getAllRequests(1L, 0, 10))
                .thenReturn(List.of(responseDto));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].description").value("Нужен молоток"));

        verify(itemRequestService).getAllRequests(1L, 0, 10);
    }

    @Test
    void getRequestByIdTest() throws Exception {
        when(itemRequestService.getRequestById(1L, 1L))
                .thenReturn(responseDto);

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Нужен молоток"));

        verify(itemRequestService).getRequestById(1L, 1L);
    }
}
