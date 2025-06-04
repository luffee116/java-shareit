package ru.practicum.shareit.user.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    public UserServiceImpl(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public UserDto create(UserDto user) {
        User responseFromDb = userStorage.create(UserMapper.toUser(user));
        return UserMapper.toUserDto(responseFromDb);
    }

    @Override
    public UserDto update(Long id, UserDto user) {
        User responseFromDb = userStorage.update(id, UserMapper.toUser(user));
        return UserMapper.toUserDto(responseFromDb);
    }

    @Override
    public UserDto get(Long userId) {
        User responseFromDb = userStorage.getUser(userId);
        return UserMapper.toUserDto(responseFromDb);
    }

    @Override
    public void delete(Long userId) {
        userStorage.delete(userId);
    }

    @Override
    public List<UserDto> getAll() {
        List<User> responseList = userStorage.getAllUsers();
        return responseList
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }
}
