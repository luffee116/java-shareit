package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.exceptions.UpdateException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        checkOwnerExist(ownerId);
        Item item = ItemMapper.toItem(itemDto, ownerId);
        Item savedItem = itemStorage.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId) {
        Item existingItem = itemStorage.findById(itemId);

        checkOwnerExist(ownerId);
        checkItemExist(existingItem);
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
        Item updatedItem = itemStorage.update(existingItem);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        Item item = itemStorage.findById(itemId);
        if (item == null) {
            throw new NotFoundException("Item not found with id " + itemId);
        }
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAllItemsByOwner(Long ownerId) {
        checkOwnerExist(ownerId);
        return itemStorage.findAllByOwnerId(ownerId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text.isBlank()) {
            return List.of();
        }
        return itemStorage.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteItem(Long itemId) {
        checkItemExist(itemStorage.findById(itemId));
        itemStorage.deleteById(itemId);
    }

    private void checkOwnerExist(Long ownerId) {
        userStorage.checkUserExist(ownerId, "User not found with id " + ownerId);
    }

    private void checkItemOwner(Long ownerId, Item item) {
        if (!ownerId.equals(item.getOwnerId())) {
            throw new UpdateException("У вещи другой владелец");
        }
    }

    private void checkItemExist(Item item) {
        if (item == null) {
            throw new NotFoundException("Item not found");
        }
    }
}
