package ru.practicum.shareit.storageTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.storage.UserStorage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserStorageTest {

    @Test
    void checkUserExists_shouldNotThrow_whenUserExists() {
        UserStorage userStorage = mock(UserStorage.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

        Long userId = 1L;

        when(userStorage.existsById(userId)).thenReturn(true);

        assertDoesNotThrow(() -> userStorage.checkUserExists(userId));

        verify(userStorage).existsById(userId);
    }

    @Test
    void checkUserExists_shouldThrow_whenUserNotExists() {
        UserStorage userStorage = mock(UserStorage.class, withSettings().defaultAnswer(CALLS_REAL_METHODS));

        Long userId = 2L;

        when(userStorage.existsById(userId)).thenReturn(false);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> userStorage.checkUserExists(userId)
        );

        assertEquals("User not found with id " + userId, ex.getMessage());
        verify(userStorage).existsById(userId);
    }
}

