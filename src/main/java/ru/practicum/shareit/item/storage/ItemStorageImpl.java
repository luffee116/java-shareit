package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class ItemStorageImpl implements ItemStorage {
    private final Map<Long, Item> items = new HashMap<>();
    private Long idCounter = 1L;

    /**
     * Сохраняет предмет
     *
     * @param item предмет
     */
    @Override
    public Item save(Item item) {
        item.setId(idCounter++);
        items.put(item.getId(), item);
        return item;
    }

    /**
     * Обновляет предмет
     *
     * @param item предмет
     */
    @Override
    public Item update(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    /**
     * Поиск предмета по id
     *
     * @param id идентификатор предмет
     */
    @Override
    public Item findById(Long id) {
        return items.get(id);
    }

    @Override
    public List<Item> findAllByOwnerId(Long ownerId) {
        return items.values().stream()
                .filter(item -> ownerId.equals(item.getOwnerId()))
                .collect(Collectors.toList());
    }

    /**
     * Поиск по тексту (название или описание) предмета
     *
     * @param text текст для поиска
     */
    @Override
    public List<Item> search(String text) {
        String searchText = text.toLowerCase();
        return items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item -> item.getName().toLowerCase().contains(searchText) ||
                        item.getDescription().toLowerCase().contains(searchText))
                .collect(Collectors.toList());
    }

    /**
     * Удаление предмета
     *
     * @param id идентификатор предмета
     */
    @Override
    public void deleteById(Long id) {
        items.remove(id);
    }
}
