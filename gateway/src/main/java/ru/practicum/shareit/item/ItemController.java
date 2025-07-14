package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @Valid @RequestBody ItemRequestDto itemDto) {
        return itemClient.create(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody ItemRequestDto itemDto) {
        return itemClient.update(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId) {
        return itemClient.getById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByUser(
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemClient.getAllByUser(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam @NotBlank String text) {
        return itemClient.search(text);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> delete(@PathVariable("itemId") Long itemId) {
        return itemClient.delete(itemId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(@PathVariable("itemId") Long itemId,
                                                @RequestHeader("X-Sharer-User-Id") Long userId,
                                                @RequestBody @Valid CommentDto commentDto) {
        return itemClient.createComment(userId, itemId, commentDto);
    }

}
