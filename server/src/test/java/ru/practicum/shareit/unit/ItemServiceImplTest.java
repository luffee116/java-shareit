package ru.practicum.shareit.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.UpdateException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemStorage itemStorage;

    @Mock
    private UserStorage userStorage;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    private final Long ownerId = 1L;
    private final Long itemId = 10L;

    private Item item;

    @BeforeEach
    void setUp() {
        item = Item.builder()
                .id(itemId)
                .name("Item1")
                .description("Desc1")
                .available(true)
                .ownerId(ownerId)
                .build();
    }

    @Test
    void createItem_ShouldSaveItem() {
        ItemDto itemDto = ItemDto.builder()
                .name("Item1")
                .description("Desc1")
                .available(true)
                .build();

        when(itemStorage.save(any())).thenReturn(item);

        ItemDto result = itemService.createItem(itemDto, ownerId);

        assertNotNull(result);
        assertEquals(item.getName(), result.getName());
        verify(userStorage).checkUserExists(ownerId);
        verify(itemStorage).save(any(Item.class));
    }

    @Test
    void updateItem_ShouldUpdateFields() {
        ItemDto updateDto = ItemDto.builder()
                .name("UpdatedName")
                .description("UpdatedDesc")
                .available(false)
                .build();

        when(itemStorage.findById(itemId)).thenReturn(Optional.of(item));

        ItemDto updated = itemService.updateItem(itemId, updateDto, ownerId);

        assertEquals("UpdatedName", updated.getName());
        assertEquals("UpdatedDesc", updated.getDescription());
        assertEquals(false, updated.getAvailable());
    }

    @Test
    void updateItem_ShouldThrowIfNotOwner() {
        ItemDto updateDto = new ItemDto();

        when(itemStorage.findById(itemId)).thenReturn(Optional.of(item));

        Long wrongOwnerId = 2L;

        UpdateException ex = assertThrows(UpdateException.class,
                () -> itemService.updateItem(itemId, updateDto, wrongOwnerId));

        assertEquals("У вещи другой владелец", ex.getMessage());
    }


    @Test
    void createComment_ShouldSaveComment() {
        Long authorId = 2L;

        CommentDto commentDto = CommentDto.builder()
                .text("Good item")
                .build();

        User user = User.builder()
                .id(authorId)
                .build();

        when(itemStorage.findById(itemId)).thenReturn(Optional.of(item));
        when(userStorage.findById(authorId)).thenReturn(Optional.of(user));
        when(bookingRepository.existsByBookerIdAndItemIdAndEndBeforeAndStatus(
                eq(authorId), eq(itemId), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(true);

        Comment comment = Comment.builder()
                .id(1L)
                .text(commentDto.getText())
                .author(user)
                .item(item)
                .created(LocalDateTime.now())
                .build();

        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto savedCommentDto = itemService.createComment(authorId, itemId, commentDto);

        assertEquals(commentDto.getText(), savedCommentDto.getText());
    }

    @Test
    void createComment_ShouldThrowIfNoBooking() {
        Long authorId = 2L;

        CommentDto commentDto = CommentDto.builder()
                .text("Good item")
                .build();

        when(itemStorage.findById(itemId)).thenReturn(Optional.of(item));
        when(userStorage.findById(authorId)).thenReturn(Optional.of(User.builder().id(authorId).build()));
        when(bookingRepository.existsByBookerIdAndItemIdAndEndBeforeAndStatus(
                eq(authorId), eq(itemId), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(false);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> itemService.createComment(authorId, itemId, commentDto));

        assertEquals("User has not booked this item or booking is not completed", ex.getMessage());
    }

    @Test
    void searchItems_ShouldReturnAvailableItemsOnly() {
        Item item2 = Item.builder()
                .id(2L)
                .name("TestItem2")
                .description("Desc2")
                .available(true)
                .ownerId(ownerId)
                .build();

        when(itemStorage.search("test")).thenReturn(List.of(item, item2));

        List<ItemDtoResponse> results = itemService.searchItems("test");

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(ItemDtoResponse::getAvailable));
    }

    @Test
    void deleteItem_ShouldCallDelete() {
        when(itemStorage.findById(itemId)).thenReturn(Optional.of(item));

        itemService.deleteItem(itemId);

        verify(itemStorage).deleteById(itemId);
    }

    @Test
    void deleteItem_ShouldThrowIfNotFound() {
        when(itemStorage.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.deleteItem(itemId));
    }

}
