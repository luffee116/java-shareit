package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto createItem(
            @RequestBody @Valid ItemDto itemDto,
            @RequestHeader("X-Sharer-User-Id") Long ownerId
    ) {
        return itemService.createItem(itemDto, ownerId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(
            @PathVariable("itemId") Long itemId,
            @RequestBody ItemDto itemDto,
            @RequestHeader("X-Sharer-User-Id") Long ownerId
    ) {
        return itemService.updateItem(itemId, itemDto, ownerId);
    }

    @GetMapping("/{itemId}")
    public ItemDtoResponse getItem(@PathVariable("itemId") Long itemId,
                                   @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping
    public List<ItemDtoResponse> getAllItemsByOwner(
            @RequestHeader("X-Sharer-User-Id") Long ownerId
    ) {
        return itemService.getAllItemsByOwner(ownerId);
    }

    @GetMapping("/search")
    public List<ItemDtoResponse> searchItems(@RequestParam("text") String text) {
        return itemService.searchItems(text);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@PathVariable("itemId") Long itemId) {
        itemService.deleteItem(itemId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@PathVariable Long itemId,
                                    @RequestHeader("X-Sharer-User-Id") Long userId,
                                    @RequestBody CommentDto commentDto) {
        return itemService.createComment(userId, itemId, commentDto);
    }
}
