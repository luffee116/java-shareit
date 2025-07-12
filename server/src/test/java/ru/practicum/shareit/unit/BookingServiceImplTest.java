package ru.practicum.shareit.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.*;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ItemStorage itemStorage;

    @Mock
    private UserStorage userStorage;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User booker;
    private Item item;
    private Booking booking;

    private final Long userId = 1L;
    private final Long ownerId = 2L;
    private final Long bookingId = 100L;
    private final Long itemId = 10L;

    @BeforeEach
    void setup() {
        booker = User.builder().id(userId).build();

        item = Item.builder()
                .id(itemId)
                .ownerId(ownerId)
                .available(true)
                .build();

        booking = Booking.builder()
                .id(bookingId)
                .item(item)
                .booker(booker)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .build();
    }

    @Test
    void createBooking_Success() {
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        when(userStorage.findById(userId)).thenReturn(Optional.of(booker));
        when(itemStorage.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(bookingId);
            return b;
        });

        BookingResponseDto response = bookingService.createBooking(requestDto, userId);

        assertNotNull(response);
        assertEquals(bookingId, response.getId());
        assertEquals(BookingStatus.WAITING, response.getStatus());
        verify(bookingRepository, times(1)).save(any());
    }

    @Test
    void createBooking_ShouldThrow_WhenOwnerBooksOwnItem() {
        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        when(userStorage.findById(ownerId)).thenReturn(Optional.of(User.builder().id(ownerId).build()));
        when(itemStorage.findById(itemId)).thenReturn(Optional.of(item));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> bookingService.createBooking(requestDto, ownerId));

        assertEquals("Owner cannot book own item", ex.getMessage());
    }

    @Test
    void createBooking_ShouldThrow_WhenItemNotAvailable() {
        item.setAvailable(false);

        BookingRequestDto requestDto = BookingRequestDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        when(userStorage.findById(userId)).thenReturn(Optional.of(booker));
        when(itemStorage.findById(itemId)).thenReturn(Optional.of(item));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> bookingService.createBooking(requestDto, userId));

        assertEquals("Item is not available", ex.getMessage());
    }



    @Test
    void approveBooking_ShouldThrow_WhenNotOwner() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> bookingService.approveBooking(bookingId, userId, true)); // userId != ownerId

        assertEquals("Only owner can approve booking", ex.getMessage());
    }

    @Test
    void approveBooking_ShouldThrow_WhenStatusNotWaiting() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> bookingService.approveBooking(bookingId, ownerId, true));

        assertEquals("Booking is not waiting", ex.getMessage());
    }

    @Test
    void getBookingById_Success_ForBooker() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        BookingResponseDto dto = bookingService.getBookingById(bookingId, userId);

        assertNotNull(dto);
    }

    @Test
    void getBookingById_Success_ForOwner() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        BookingResponseDto dto = bookingService.getBookingById(bookingId, ownerId);

        assertNotNull(dto);
    }

    @Test
    void getBookingById_ShouldThrow_WhenNeitherBookerNorOwner() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        ValidationException ex = assertThrows(ValidationException.class,
                () -> bookingService.getBookingById(bookingId, 999L));

        assertEquals("Only owner and booker can get booking", ex.getMessage());
    }

    @Test
    void getUserBookings_AllState() {
        when(userStorage.findById(userId)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdOrderByStartDesc(userId)).thenReturn(List.of(booking));

        List<BookingResponseDto> bookings = bookingService.getUserBookings(userId, BookingState.ALL, 0, 10);

        assertFalse(bookings.isEmpty());
        verify(bookingRepository).findByBookerIdOrderByStartDesc(userId);
    }

    @Test
    void getOwnerBookings_AllState() {
        when(userStorage.findById(ownerId)).thenReturn(Optional.of(User.builder().id(ownerId).build()));
        when(itemStorage.countByOwnerId(ownerId)).thenReturn(1L);
        when(bookingRepository.findByItemOwnerIdOrderByStartDesc(ownerId)).thenReturn(List.of(booking));

        List<BookingResponseDto> bookings = bookingService.getOwnerBookings(ownerId, BookingState.ALL, 0, 10);

        assertFalse(bookings.isEmpty());
        verify(bookingRepository).findByItemOwnerIdOrderByStartDesc(ownerId);
    }

    @Test
    void getOwnerBookings_ShouldThrow_WhenUserOwnsNoItems() {
        when(userStorage.findById(ownerId)).thenReturn(Optional.of(User.builder().id(ownerId).build()));
        when(itemStorage.countByOwnerId(ownerId)).thenReturn(0L);

        ValidationException ex = assertThrows(ValidationException.class,
                () -> bookingService.getOwnerBookings(ownerId, BookingState.ALL, 0, 10));

        assertEquals("User with id " + ownerId + " doesn't own any items", ex.getMessage());
    }

    @Test
    void testGetUserBookings_whenStateRejected_thenReturnRejectedBookings() {
        Booking rejectedBooking1 = Booking.builder()
                .id(1L)
                .status(BookingStatus.REJECTED)
                .build();

        Booking rejectedBooking2 = Booking.builder()
                .id(2L)
                .status(BookingStatus.REJECTED)
                .build();

        when(userStorage.findById(userId)).thenReturn(Optional.of(booker));

        when(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED))
                .thenReturn(List.of(rejectedBooking1, rejectedBooking2));

        try (MockedStatic<BookingMapper> mockedMapper = mockStatic(BookingMapper.class)) {
            mockedMapper.when(() -> BookingMapper.toDto(any(Booking.class)))
                    .thenAnswer(invocation -> {
                        Booking b = invocation.getArgument(0);
                        return BookingResponseDto.builder()
                                .id(b.getId())
                                .status(b.getStatus())
                                .build();
                    });

            List<BookingResponseDto> result = bookingService.getUserBookings(userId, BookingState.REJECTED, 0, 10);

            verify(bookingRepository).findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);

            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(dto -> dto.getStatus() == BookingStatus.REJECTED));
        }
    }


    @Test
    void testGetUserBookings_whenStateWaiting_thenReturnWaitingBookings() {
        Long userId = 2L;

        Booking waitingBooking = Booking.builder()
                .id(10L)
                .status(BookingStatus.WAITING)
                .build();

        when(userStorage.findById(userId)).thenReturn(Optional.of(User.builder().id(userId).build()));

        when(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING))
                .thenReturn(List.of(waitingBooking));

        try (MockedStatic<BookingMapper> mockedMapper = mockStatic(BookingMapper.class)) {
            mockedMapper.when(() -> BookingMapper.toDto(any(Booking.class)))
                    .thenAnswer(invocation -> {
                        Booking b = invocation.getArgument(0);
                        return BookingResponseDto.builder()
                                .id(b.getId())
                                .status(b.getStatus())
                                .build();
                    });

            List<BookingResponseDto> result = bookingService.getUserBookings(userId, BookingState.WAITING, 0, 10);

            verify(bookingRepository).findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            assertEquals(1, result.size());
            assertEquals(BookingStatus.WAITING, result.getFirst().getStatus());
            assertEquals(10L, result.getFirst().getId());
        }
    }


}
