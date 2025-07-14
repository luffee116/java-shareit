package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exceptions.UserException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Override
    @Transactional
    public UserDto create(UserDto user) {
        if (userStorage.existsByEmailAndIdNot(user.getEmail(), user.getId())) {
            throw new UserException("User with email " + user.getEmail() + " already exists");
        }

        User savedUser = userStorage.save(UserMapper.toUser(user));
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto update(Long id, UserDto userDto) {
        User existingUser = checkUserExists(id);

        if (userDto.getEmail() != null &&
                userStorage.existsByEmailAndIdNot(userDto.getEmail(), id)) {
            throw new UserException("User with email " + userDto.getEmail() + " already exists");
        }

        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            existingUser.setEmail(userDto.getEmail());
        }
        return UserMapper.toUserDto(existingUser);
    }

    @Override
    public UserDto get(Long userId) {
        User existingUser = checkUserExists(userId);
        return UserMapper.toUserDto(existingUser);
    }

    @Override
    public void delete(Long userId) {
        if (!userStorage.existsById(userId)) {
            throw new UserException("User with id " + userId + " not found");
        }
        userStorage.deleteById(userId);
    }

    @Override
    public List<UserDto> getAll() {
        return userStorage
                .findAll()
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public User checkUserExists(Long userId) {
        return userStorage.findById(userId).orElseThrow(() -> new UserException("User with id " + userId + " not found"));
    }
}
