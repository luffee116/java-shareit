package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoResponse;

import java.util.List;

public interface ItemService {
    ItemDto createItem(ItemDto itemDto, Long ownerId);

    ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId);

    ItemDtoResponse getItemById(Long itemId, Long userId);

    List<ItemDtoResponse> getAllItemsByOwner(Long ownerId);

    List<ItemDtoResponse> searchItems(String text);

    void deleteItem(Long itemId);

    CommentDto createComment(Long authorId, Long itemId, CommentDto commentDto);

}
