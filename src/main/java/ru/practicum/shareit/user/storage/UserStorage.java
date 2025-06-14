package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserStorage {

    User create(User user);

    User update(Long id, User user);

    void delete(Long userID);

    User getUser(Long userId);

    List<User> getAllUsers();

    void checkUserExist(Long ownerId, String message);
}
