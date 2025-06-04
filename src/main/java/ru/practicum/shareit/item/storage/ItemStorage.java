package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    Item save(Item item);

    Item update(Item item);

    Item findById(Long id);

    List<Item> findAllByOwnerId(Long ownerId);

    List<Item> search(String text);

    void deleteById(Long id);
}
