package ru.practicum.shareit.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    private ItemDto itemDto;
    private ItemDtoResponse itemDtoResponse;
    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        itemDto = ItemDto.builder()
                .id(1L)
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .requestId(null)
                .build();

        itemDtoResponse = ItemDtoResponse.builder()
                .id(1L)
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .lastBooking(null)
                .nextBooking(null)
                .comments(List.of())
                .build();

        commentDto = CommentDto.builder()
                .id(1L)
                .text("Отличная вещь!")
                .authorName("Иван")
                .created(LocalDateTime.of(2025, 7, 20, 12, 0))
                .build();
    }

    @Test
    void createItemTest() throws Exception {
        when(itemService.createItem(any(ItemDto.class), eq(1L)))
                .thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Дрель"));

        verify(itemService).createItem(any(ItemDto.class), eq(1L));
    }

    @Test
    void updateItemTest() throws Exception {
        when(itemService.updateItem(eq(1L), any(ItemDto.class), eq(1L)))
                .thenReturn(itemDto);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Дрель"));

        verify(itemService).updateItem(eq(1L), any(ItemDto.class), eq(1L));
    }

    @Test
    void getItemTest() throws Exception {
        when(itemService.getItemById(1L, 1L))
                .thenReturn(itemDtoResponse);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Дрель"));

        verify(itemService).getItemById(1L, 1L);
    }

    @Test
    void getAllItemsByOwnerTest() throws Exception {
        when(itemService.getAllItemsByOwner(1L))
                .thenReturn(List.of(itemDtoResponse));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Дрель"));

        verify(itemService).getAllItemsByOwner(1L);
    }

    @Test
    void searchItemsTest() throws Exception {
        when(itemService.searchItems("дрель"))
                .thenReturn(List.of(itemDtoResponse));

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Дрель"));

        verify(itemService).searchItems("дрель");
    }

    @Test
    void deleteItemTest() throws Exception {
        doNothing().when(itemService).deleteItem(1L);

        mockMvc.perform(delete("/items/1"))
                .andExpect(status().isOk());

        verify(itemService).deleteItem(1L);
    }

    @Test
    void createCommentTest() throws Exception {
        when(itemService.createComment(eq(1L), eq(1L), any(CommentDto.class)))
                .thenReturn(commentDto);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Отличная вещь!"))
                .andExpect(jsonPath("$.authorName").value("Иван"));

        verify(itemService).createComment(eq(1L), eq(1L), any(CommentDto.class));
    }
}
