package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.UpdateException;
import ru.practicum.shareit.exceptions.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.mapper.BookingMapper;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        checkOwnerExist(ownerId);
        Item item = ItemMapper.toItem(itemDto, ownerId);
        Item savedItem = itemStorage.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId) {
        Item existingItem = itemStorage.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        checkOwnerExist(ownerId);
        checkItemOwner(ownerId, existingItem);

        // Обновляем только не-null поля из DTO
        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }
        return ItemMapper.toItemDto(existingItem);
    }

    @Override
    public ItemDtoResponse getItemById(Long itemId, Long userId) {
        Item item = itemStorage.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        if (!item.getOwnerId().equals(userId)) {
            Collection<Booking> bookings = bookingRepository.findByItemId(item.getId());
            Collection<Comment> comments = commentRepository.findAllByItemId(item.getId());
            return ItemDtoResponse.builder()
                    .id(item.getId())
                    .name(item.getName())
                    .description(item.getDescription())
                    .available(item.getAvailable())
                    .bookings(bookings.stream().map(BookingMapper::toDto).toList())
                    .comments(comments.stream().map(CommentMapper::toDto).toList())
                    .build();
        }
        return addBookingsAndCommentsInfo(item);
    }

    @Override
    public List<ItemDto> getAllItemsByOwner(Long ownerId) {
        checkOwnerExist(ownerId);
        return itemStorage.findAllByOwnerId(ownerId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDtoResponse> searchItems(String text) {
        if (text.trim().isEmpty()) {
            return Collections.emptyList();
        }
        List<Item> items = itemStorage.search(text).stream()
                .filter(Item::getAvailable)
                .toList();

        return items.stream().map(this::addBookingsAndCommentsInfo).toList();
    }

    @Override
    public void deleteItem(Long itemId) {
        if (!itemStorage.existsById(itemId)) {
            throw new NotFoundException("Item not found");
        }
        itemStorage.deleteById(itemId);
    }

    @Override
    public CommentDto createComment(Long authorId, Long itemId, CommentDto commentDto) {
        Item item = itemStorage.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));
        User user = userStorage.findById(authorId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        boolean hasBooked = bookingRepository.
                existsByBookerIdAndItemIdAndEndBefore(authorId, itemId, LocalDateTime.now());

        if (!hasBooked) {
            throw new ValidationException("User has not booked this item or booking is not completed");
        }

        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .author(user)
                .item(item)
                .created(LocalDateTime.now())
                .build();

        Comment savedComment = commentRepository.save(comment);

        return CommentMapper.toDto(savedComment);
    }

    private void checkOwnerExist(Long ownerId) {
        userStorage.checkUserExists(ownerId);
    }

    private void checkItemOwner(Long ownerId, Item item) {
        if (!ownerId.equals(item.getOwnerId())) {
            throw new UpdateException("У вещи другой владелец");
        }
    }


    private ItemDtoResponse addBookingsAndCommentsInfo(Item item) {
        Collection<Booking> bookings = bookingRepository.findByItemId(item.getId());

        LocalDateTime lastBookingDate = bookings.stream()
                .map(Booking::getEnd)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime nextBookingDate = bookings.stream()
                .map(Booking::getStart)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        ItemDtoResponse itemDtoResponse = ItemDtoResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .build();

        itemDtoResponse.setLastBooking(lastBookingDate);
        itemDtoResponse.setNextBooking(nextBookingDate);
        itemDtoResponse.setComments(getCommentsForItem(item.getId()));
        return itemDtoResponse;
    }

    private List<CommentDto> getCommentsForItem(Long itemId) {
        return commentRepository.findAllByItemId(itemId).stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());
    }
}
