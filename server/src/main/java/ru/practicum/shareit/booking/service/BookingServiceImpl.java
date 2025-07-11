package ru.practicum.shareit.booking.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    @Transactional
    public BookingResponseDto createBooking(BookingRequestDto bookingDto, Long userId) {
        User booker = checkUserExist(userId);
        Item item = checkItemExist(bookingDto.getItemId());

        validateBookingRequest(item, userId);

        Booking booking = BookingMapper.toBooking(bookingDto);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);


        Booking savedBooking = bookingRepository.save(booking);
        return BookingMapper.toDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto approveBooking(Long bookingId, Long ownerId, boolean approved) {
        Booking booking = checkBookingExist(bookingId);

        if (!booking.getItem().getOwnerId().equals(ownerId)) {
            throw new ValidationException("Only owner can approve booking");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Booking is not waiting");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking savedBooking = bookingRepository.save(booking);
        return BookingMapper.toDto(savedBooking);
    }

    @Override
    public BookingResponseDto getBookingById(Long bookingId, Long userId) {
        Booking booking = checkBookingExist(bookingId);

        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwnerId().equals(userId)) {
            throw new ValidationException("Only owner and booker can get booking");
        }

        return BookingMapper.toDto(booking);
    }

    @Override
    public List<BookingResponseDto> getUserBookings(Long userId, BookingState state, int from, int size) {
        checkUserExist(userId);
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case ALL:
                return bookingRepository
                        .findByBookerIdOrderByStartDesc(userId)
                        .stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case CURRENT:
                return bookingRepository
                        .findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now)
                        .stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case PAST:
                return bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId, now)
                        .stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case FUTURE:
                return bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, now)
                        .stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case WAITING:
                return bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING)
                        .stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case REJECTED:
                return bookingRepository.findByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED)
                        .stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            default:
                throw new ValidationException("Unknown state");
        }

    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long ownerId, BookingState state, int from, int size) {
        userStorage.findById(ownerId).orElseThrow(() -> new NotFoundException("User not found"));

        if (itemStorage.countByOwnerId(ownerId) == 0) {
            throw new ValidationException("User with id " + ownerId + " doesn't own any items");
        }

        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case ALL:
                return bookingRepository
                        .findByItemOwnerIdOrderByStartDesc(ownerId)
                        .stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case CURRENT:
                return bookingRepository
                        .findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(ownerId, now, now)
                        .stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case PAST:
                return bookingRepository
                        .findByItemOwnerIdAndEndBeforeOrderByStartDesc(ownerId, now)
                        .stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case FUTURE:
                return bookingRepository
                        .findByItemOwnerIdAndStartAfterOrderByStartDesc(ownerId, now)
                        .stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case WAITING:
                return bookingRepository
                        .findByItemOwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.WAITING)
                        .stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            case REJECTED:
                return bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.REJECTED)
                        .stream()
                        .map(BookingMapper::toDto)
                        .collect(Collectors.toList());
            default:
                throw new ValidationException("Unknown state");
        }
    }

    private void validateBookingRequest(Item item, Long userId) {
        if (item.getOwnerId().equals(userId)) {
            throw new ValidationException("Owner cannot book own item");
        }
        if (!item.getAvailable()) {
            throw new ValidationException("Item is not available");
        }
    }

    private User checkUserExist(Long userId) {
        return userStorage.findById(userId).orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
    }

    private Item checkItemExist(Long itemId) {
        return itemStorage.findById(itemId).orElseThrow(() -> new NotFoundException("Item not found with id: " + itemId));
    }

    private Booking checkBookingExist(Long bookingId) {
        return bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundException("Booking not found with id: " + bookingId));
    }
}
