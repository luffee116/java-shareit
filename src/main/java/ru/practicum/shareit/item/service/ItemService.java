package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto createItem(ItemDto itemDto, Long ownerId);

    ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId);

    ItemDto getItemById(Long itemId);

    List<ItemDto> getAllItemsByOwner(Long ownerId);

    List<ItemDto> searchItems(String text);

    void deleteItem(Long itemId);

}
