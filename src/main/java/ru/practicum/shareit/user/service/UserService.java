package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto create(UserDto user);

    UserDto update(Long userId, UserDto user);

    UserDto get(Long userId);

    void delete(Long userId);

    List<UserDto> getAll();
}
