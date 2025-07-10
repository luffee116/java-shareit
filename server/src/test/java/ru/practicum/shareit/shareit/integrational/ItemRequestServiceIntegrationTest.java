package ru.practicum.shareit.shareit.integrational;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestInputDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({ItemRequestServiceImpl.class, UserServiceImpl.class, ItemServiceImpl.class})
class ItemRequestServiceIntegrationTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ItemRequestRepository requestRepository;

    @Autowired
    private UserStorage userStorage;

    @Autowired
    private ItemStorage itemStorage;

    @Autowired
    private ItemRequestServiceImpl requestService;

    private User requestor;
    private User otherUser;
    private ItemRequest request;

    @BeforeEach
    void setUp() {
        requestor = new User(null, "Requestor", "requestor@example.com");
        otherUser = new User(null, "Other User", "other@example.com");
        em.persist(requestor);
        em.persist(otherUser);
        em.flush();

        request = ItemRequest.builder()
                .description("Need item")
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();
        em.persist(request);
        em.flush();
    }

    @Test
    void createRequest_ShouldSaveRequestToDatabase() {
        // Given
        ItemRequestInputDto inputDto = new ItemRequestInputDto();
        inputDto.setDescription("New request");

        // When
        ItemRequestDto createdRequest = requestService.createRequest(requestor.getId(), inputDto);

        // Then
        assertNotNull(createdRequest.getId());
        assertEquals("New request", createdRequest.getDescription());
        assertEquals(requestor.getId(), createdRequest.getRequestorId());

        ItemRequest dbRequest = em.find(ItemRequest.class, createdRequest.getId());
        assertThat(dbRequest).isNotNull();
        assertThat(dbRequest.getDescription()).isEqualTo("New request");
    }

    @Test
    void getUserRequests_ShouldReturnUserRequests() {
        // Given
        ItemRequest secondRequest = ItemRequest.builder()
                .description("Second request")
                .requestor(requestor)
                .created(LocalDateTime.now().plusHours(1))
                .build();
        em.persist(secondRequest);
        em.flush();

        // When
        List<ItemRequestDto> requests = requestService.getUserRequests(requestor.getId());

        // Then
        assertThat(requests).hasSize(2);
        assertThat(requests.get(0).getDescription()).isEqualTo("Second request"); // Проверка сортировки по дате
        assertThat(requests.get(1).getDescription()).isEqualTo("Need item");
    }

    @Test
    void getAllRequests_ShouldReturnOtherUsersRequests() {
        // Given
        ItemRequest otherUserRequest = ItemRequest.builder()
                .description("Other user request")
                .requestor(otherUser)
                .created(LocalDateTime.now().plusHours(1))
                .build();
        em.persist(otherUserRequest);
        em.flush();

        // When
        List<ItemRequestDto> requests = requestService.getAllRequests(
                requestor.getId(), 0, 10);

        // Then
        assertThat(requests).hasSize(1);
        assertThat(requests.getFirst().getDescription()).isEqualTo("Other user request");
    }

    @Test
    void getAllRequests_WithPagination_ShouldReturnCorrectPage() {
        // Given - создаем несколько запросов от других пользователей
        for (int i = 1; i <= 5; i++) {
            User user = new User(null, "User " + i, "user" + i + "@example.com");
            em.persist(user);

            ItemRequest req = ItemRequest.builder()
                    .description("Request " + i)
                    .requestor(user)
                    .created(LocalDateTime.now().plusHours(i))
                    .build();
            em.persist(req);
        }
        em.flush();

        // When - запрашиваем страницу 0 с размером 2
        List<ItemRequestDto> requests = requestService.getAllRequests(
                requestor.getId(), 0, 2);

        // Then
        assertThat(requests).hasSize(2);
        assertThat(requests.get(0).getDescription()).isEqualTo("Request 5"); // Сортировка по дате (DESC)
        assertThat(requests.get(1).getDescription()).isEqualTo("Request 4");
    }

    @Test
    void getRequestById_ShouldReturnRequestWithItems() {
        // Given - добавляем вещь к запросу
        Item item = Item.builder()
                .name("Item for request")
                .description("Description")
                .available(true)
                .ownerId(otherUser.getId())
                .request(request)
                .build();
        em.persist(item);
        em.flush();

        // When
        ItemRequestDto foundRequest = requestService.getRequestById(otherUser.getId(), request.getId());

        // Then
        assertEquals(request.getId(), foundRequest.getId());
        assertEquals("Need item", foundRequest.getDescription());
        assertThat(foundRequest.getItems()).hasSize(1);
        assertThat(foundRequest.getItems().getFirst().getName()).isEqualTo("Item for request");
    }

    @Test
    void getRequestById_WhenRequestNotFound_ShouldThrowException() {
        // When & Then
        assertThrows(NotFoundException.class, () ->
                requestService.getRequestById(requestor.getId(), 999L));
    }

    @Test
    void convertToDto_ShouldIncludeItems() {
        // Given
        Item item1 = Item.builder()
                .name("Item 1")
                .description("Desc 1")
                .available(true)
                .ownerId(otherUser.getId())
                .request(request)
                .build();

        Item item2 = Item.builder()
                .name("Item 2")
                .description("Desc 2")
                .available(true)
                .ownerId(otherUser.getId())
                .request(request)
                .build();

        em.persist(item1);
        em.persist(item2);
        em.flush();

        // When
        ItemRequestDto dto = requestService.getRequestById(otherUser.getId(), request.getId());

        // Then
        assertThat(dto.getItems()).hasSize(2);
        assertThat(dto.getItems()).extracting(ItemDto::getName)
                .containsExactlyInAnyOrder("Item 1", "Item 2");
    }
}