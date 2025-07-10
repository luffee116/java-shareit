package ru.practicum.shareit.shareit.integrational;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(BookingServiceImpl.class)
class BookingServiceIntegrationTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemStorage itemStorage;

    @Autowired
    private UserStorage userStorage;

    @Autowired
    private BookingServiceImpl bookingService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;

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

        booking = Booking.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        em.persist(booking);
        em.flush();
    }

    @Test
    void createBooking_ShouldSaveBookingToDatabase() {
        // Given
        BookingRequestDto bookingDto = new BookingRequestDto(
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(4),
                item.getId()
        );

        // When
        BookingResponseDto savedBooking = bookingService.createBooking(bookingDto, booker.getId());

        // Then
        assertNotNull(savedBooking.getId());
        assertEquals(booker.getId(), savedBooking.getBooker().getId());
        assertEquals(item.getId(), savedBooking.getItem().getId());
        assertEquals(BookingStatus.WAITING, savedBooking.getStatus());

        Booking dbBooking = em.find(Booking.class, savedBooking.getId());
        assertThat(dbBooking).isNotNull();
        assertThat(dbBooking.getBooker().getId()).isEqualTo(booker.getId());
    }

    @Test
    void createBooking_WhenBookOwnItem_ShouldThrowException() {
        // Given
        BookingRequestDto bookingDto = new BookingRequestDto(
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(4),
                item.getId()
        );

        // When & Then
        assertThrows(ValidationException.class, () ->
                bookingService.createBooking(bookingDto, owner.getId()));
    }

    @Test
    void approveBooking_ShouldUpdateStatus() {
        // When
        BookingResponseDto approvedBooking = bookingService.approveBooking(booking.getId(), owner.getId(), true);

        // Then
        assertEquals(BookingStatus.APPROVED, approvedBooking.getStatus());

        Booking dbBooking = em.find(Booking.class, booking.getId());
        assertThat(dbBooking.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void approveBooking_WhenNotOwner_ShouldThrowException() {
        // When & Then
        assertThrows(ValidationException.class, () ->
                bookingService.approveBooking(booking.getId(), booker.getId(), true));
    }

    @Test
    void getBookingById_ShouldReturnBooking() {
        // When
        BookingResponseDto foundBooking = bookingService.getBookingById(booking.getId(), booker.getId());

        // Then
        assertEquals(booking.getId(), foundBooking.getId());
        assertEquals(booker.getId(), foundBooking.getBooker().getId());
        assertEquals(item.getId(), foundBooking.getItem().getId());
    }

    @Test
    void getBookingById_WhenNotOwnerOrBooker_ShouldThrowException() {
        // Given
        User otherUser = new User(null, "Other", "other@example.com");
        em.persist(otherUser);
        em.flush();

        // When & Then
        assertThrows(ValidationException.class, () ->
                bookingService.getBookingById(booking.getId(), otherUser.getId()));
    }

    @Test
    void getUserBookings_ShouldReturnBookingsForAllState() {
        // Given
        Booking pastBooking = Booking.builder()
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
        em.persist(pastBooking);
        em.flush();

        // When
        List<BookingResponseDto> allBookings = bookingService.getUserBookings(
                booker.getId(), BookingState.ALL, 0, 10);
        List<BookingResponseDto> pastBookings = bookingService.getUserBookings(
                booker.getId(), BookingState.PAST, 0, 10);
        List<BookingResponseDto> currentBookings = bookingService.getUserBookings(
                booker.getId(), BookingState.CURRENT, 0, 10);
        List<BookingResponseDto> futureBookings = bookingService.getUserBookings(
                booker.getId(), BookingState.FUTURE, 0, 10);

        // Then
        assertThat(allBookings).hasSize(2);
        assertThat(pastBookings).hasSize(1);
        assertThat(currentBookings).isEmpty();
        assertThat(futureBookings).hasSize(1);
    }

    @Test
    void getOwnerBookings_ShouldReturnBookingsForAllState() {
        // Given
        Booking pastBooking = Booking.builder()
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
        em.persist(pastBooking);
        em.flush();

        // When
        List<BookingResponseDto> allBookings = bookingService.getOwnerBookings(
                owner.getId(), BookingState.ALL, 0, 10);
        List<BookingResponseDto> pastBookings = bookingService.getOwnerBookings(
                owner.getId(), BookingState.PAST, 0, 10);
        List<BookingResponseDto> currentBookings = bookingService.getOwnerBookings(
                owner.getId(), BookingState.CURRENT, 0, 10);
        List<BookingResponseDto> futureBookings = bookingService.getOwnerBookings(
                owner.getId(), BookingState.FUTURE, 0, 10);

        // Then
        assertThat(allBookings).hasSize(2);
        assertThat(pastBookings).hasSize(1);
        assertThat(currentBookings).isEmpty();
        assertThat(futureBookings).hasSize(1);
    }

    @Test
    void getOwnerBookings_WhenNoItemsOwned_ShouldThrowException() {
        // Given
        User nonOwner = new User(null, "NonOwner", "nonowner@example.com");
        em.persist(nonOwner);
        em.flush();

        // When & Then
        assertThrows(ValidationException.class, () ->
                bookingService.getOwnerBookings(nonOwner.getId(), BookingState.ALL, 0, 10));
    }
}