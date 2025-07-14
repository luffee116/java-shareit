package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody UserRequestDto userRequestDto) {
        return userClient.create(userRequestDto);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> update(
            @PathVariable Long userId,
            @RequestBody @Valid UserUpdateDto userUpdateDto) {
        return userClient.update(userId, userUpdateDto);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getById(@PathVariable Long userId) {
        return userClient.getById(userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAll() {
        return userClient.getAll();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> delete(@PathVariable Long userId) {
        return userClient.delete(userId);
    }
}
