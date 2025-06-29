package ru.practicum.shareit.user.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.model.User;

public interface UserStorage extends JpaRepository<User, Long> {

    boolean existsByEmailAndIdNot(String email, Long excludeId);

    default void checkUserExists(Long userId) {
        if (!existsById(userId)) {
            throw new NotFoundException("User not found with id " + userId);
        }
    }
}