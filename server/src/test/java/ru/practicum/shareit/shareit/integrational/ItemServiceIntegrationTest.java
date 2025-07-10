package ru.practicum.shareit.shareit.integrational;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(ItemServiceImpl.class)
class ItemServiceIntegrationTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ItemStorage itemStorage;

    @Autowired
    private UserStorage userStorage;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ItemServiceImpl itemService;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = new User(null, "Owner", "owner@example.com");
        booker = new User(null, "Booker", "booker@example.com");
        em.persist(owner);
        em.persist(booker);
        em.flush();

        item = new Item(null, "Item", "Description", true, owner.getId(), null);
        em.persist(item);
        em.flush();
    }

    @Test
    void createItem_ShouldSaveItemToDatabase() {
        // Given
        ItemDto itemDto = new ItemDto(null, "New Item", "New Description", true, null);

        // When
        ItemDto savedItem = itemService.createItem(itemDto, owner.getId());

        // Then
        assertNotNull(savedItem.getId());
        assertEquals("New Item", savedItem.getName());
        assertEquals("New Description", savedItem.getDescription());

        Item dbItem = em.find(Item.class, savedItem.getId());
        assertThat(dbItem).isNotNull();
        assertThat(dbItem.getOwnerId()).isEqualTo(owner.getId());
    }

    @Test
    void updateItem_ShouldUpdateFields() {
        // Given
        ItemDto updateDto = new ItemDto(item.getId(), "Updated Name", "Updated Desc", false, null);

        // When
        ItemDto updatedItem = itemService.updateItem(item.getId(), updateDto, owner.getId());

        // Then
        assertEquals(item.getId(), updatedItem.getId());
        assertEquals("Updated Name", updatedItem.getName());
        assertEquals("Updated Desc", updatedItem.getDescription());
        assertFalse(updatedItem.getAvailable());

        Item dbItem = em.find(Item.class, item.getId());
        assertThat(dbItem.getName()).isEqualTo("Updated Name");
        assertThat(dbItem.getDescription()).isEqualTo("Updated Desc");
        assertThat(dbItem.getAvailable()).isFalse();
    }

    @Test
    void updateItem_WithPartialUpdate_ShouldUpdateOnlySpecifiedFields() {
        // Given
        ItemDto updateDto = new ItemDto();
        updateDto.setId(item.getId());
        updateDto.setName("Updated Name");

        // When
        ItemDto updatedItem = itemService.updateItem(item.getId(), updateDto, owner.getId());

        // Then
        assertEquals(item.getId(), updatedItem.getId());
        assertEquals("Updated Name", updatedItem.getName());
        assertEquals("Description", updatedItem.getDescription()); // Осталось прежним
        assertTrue(updatedItem.getAvailable()); // Осталось прежним
    }

    @Test
    void getItemById_ShouldReturnItemWithDetails() {
        // Given
        Booking pastBooking = new Booking(null, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1),
                item, booker, BookingStatus.APPROVED);
        Booking futureBooking = new Booking(null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                item, booker, BookingStatus.APPROVED);
        em.persist(pastBooking);
        em.persist(futureBooking);
        em.flush();

        Comment comment = new Comment(null, "Great item!", item, booker, LocalDateTime.now());
        em.persist(comment);
        em.flush();

        // When
        ItemDtoResponse itemDto = itemService.getItemById(item.getId(), owner.getId());

        // Then
        assertEquals(item.getId(), itemDto.getId());
        assertEquals("Item", itemDto.getName());
        assertThat(itemDto.getLastBooking()).isNotNull();
        assertThat(itemDto.getNextBooking()).isNotNull();
        assertThat(itemDto.getComments()).hasSize(1);
        assertThat(itemDto.getComments().getFirst().getText()).isEqualTo("Great item!");
    }

    @Test
    void getAllItemsByOwner_ShouldReturnItemsWithBookingsAndComments() {
        // Given
        Booking pastBooking = new Booking(null, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1),
                item, booker, BookingStatus.APPROVED);
        em.persist(pastBooking);
        em.flush();

        Comment comment = new Comment(null, "Nice!", item, booker, LocalDateTime.now());
        em.persist(comment);
        em.flush();

        // When
        List<ItemDtoResponse> items = itemService.getAllItemsByOwner(owner.getId());

        // Then
        assertThat(items).hasSize(1);
        ItemDtoResponse itemDto = items.getFirst();
        assertThat(itemDto.getLastBooking()).isNotNull();
        assertThat(itemDto.getComments()).hasSize(1);
        assertThat(itemDto.getComments().getFirst().getText()).isEqualTo("Nice!");
    }

    @Test
    void searchItems_ShouldReturnAvailableItemsContainingText() {
        // Given
        Item item2 = new Item(null, "Another item", "Searchable text", true, owner.getId(), null);
        em.persist(item2);
        em.flush();

        // When
        List<ItemDtoResponse> foundItems = itemService.searchItems("search");

        // Then
        assertThat(foundItems).hasSize(1);
        assertThat(foundItems.getFirst()).extracting(ItemDtoResponse::getDescription).isEqualTo("Searchable text");
    }

    @Test
    void searchItems_WithEmptyText_ShouldReturnEmptyList() {
        // When
        List<ItemDtoResponse> foundItems = itemService.searchItems(" ");

        // Then
        assertThat(foundItems).isEmpty();
    }

    @Test
    void deleteItem_ShouldRemoveItemFromDatabase() {
        // When
        itemService.deleteItem(item.getId());

        // Then
        assertNull(em.find(Item.class, item.getId()));
    }

    @Test
    void createComment_ShouldSaveCommentToDatabase() {
        // Given
        Booking booking = new Booking(null, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1),
                item, booker, BookingStatus.APPROVED);
        em.persist(booking);
        em.flush();

        CommentDto commentDto = new CommentDto(null, "Great item!", null, null);

        // When
        CommentDto savedComment = itemService.createComment(booker.getId(), item.getId(), commentDto);

        // Then
        assertNotNull(savedComment.getId());
        assertEquals("Great item!", savedComment.getText());
        assertEquals("Booker", savedComment.getAuthorName());

        Comment dbComment = em.find(Comment.class, savedComment.getId());
        assertThat(dbComment).isNotNull();
        assertThat(dbComment.getText()).isEqualTo("Great item!");
    }

    @Test
    void getItemsByRequestId_ShouldReturnItemsWithRequestId() {
        // Given
        ItemRequest request = new ItemRequest(null, "Need item", booker, LocalDateTime.now());
        em.persist(request);
        em.flush();

        Item itemWithRequest = new Item(null, "Requested Item", "Desc", true, owner.getId(), request);
        em.persist(itemWithRequest);
        em.flush();

        // When
        List<ItemDto> items = itemService.getItemsByRequestId(request.getId());

        // Then
        assertThat(items).hasSize(1);
        assertThat(items.getFirst().getName()).isEqualTo("Requested Item");
    }
}