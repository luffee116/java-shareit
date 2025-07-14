package ru.practicum.shareit.integrational;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.exceptions.UserException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(UserServiceImpl.class)
class UserServiceIntegrationTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserStorage userStorage;

    @Autowired
    private UserServiceImpl userService;

    @Test
    void createUser_ShouldSaveUserToDatabase() {
        // Given
        UserDto userDto = new UserDto(null, "John Doe", "john@example.com");

        // When
        UserDto savedUser = userService.create(userDto);

        // Then
        assertNotNull(savedUser.getId());
        assertEquals("John Doe", savedUser.getName());
        assertEquals("john@example.com", savedUser.getEmail());

        User dbUser = em.find(User.class, savedUser.getId());
        assertThat(dbUser).isNotNull();
        assertThat(dbUser.getName()).isEqualTo("John Doe");
    }

    @Test
    void updateUser_ShouldUpdateFields() {
        // Given
        User existingUser = new User(null, "Original Name", "original@example.com");
        em.persist(existingUser);
        em.flush();

        UserDto updateDto = new UserDto(existingUser.getId(), "Updated Name", "updated@example.com");

        // When
        UserDto updatedUser = userService.update(existingUser.getId(), updateDto);

        // Then
        assertEquals(existingUser.getId(), updatedUser.getId());
        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("updated@example.com", updatedUser.getEmail());

        User dbUser = em.find(User.class, existingUser.getId());
        assertThat(dbUser.getName()).isEqualTo("Updated Name");
        assertThat(dbUser.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    void updateUser_WithPartialUpdate_ShouldUpdateOnlySpecifiedFields() {
        // Given
        User existingUser = new User(null, "Original Name", "original@example.com");
        em.persist(existingUser);
        em.flush();

        UserDto updateDto = new UserDto();
        updateDto.setId(existingUser.getId());
        updateDto.setName("Updated Name");

        // When
        UserDto updatedUser = userService.update(existingUser.getId(), updateDto);

        // Then
        assertEquals(existingUser.getId(), updatedUser.getId());
        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("original@example.com", updatedUser.getEmail());

        User dbUser = em.find(User.class, existingUser.getId());
        assertThat(dbUser.getName()).isEqualTo("Updated Name");
        assertThat(dbUser.getEmail()).isEqualTo("original@example.com");
    }

    @Test
    void getUser_WhenUserExists_ShouldReturnUser() {
        // Given
        User existingUser = new User(null, "Test User", "test@example.com");
        em.persist(existingUser);
        em.flush();

        // When
        UserDto foundUser = userService.get(existingUser.getId());

        // Then
        assertEquals(existingUser.getId(), foundUser.getId());
        assertEquals("Test User", foundUser.getName());
        assertEquals("test@example.com", foundUser.getEmail());
    }

    @Test
    void getUser_WhenUserNotExists_ShouldThrowException() {
        // When & Then
        assertThrows(UserException.class, () -> userService.get(999L));
    }

    @Test
    void deleteUser_ShouldRemoveUserFromDatabase() {
        // Given
        User existingUser = new User(null, "To Delete", "delete@example.com");
        em.persist(existingUser);
        em.flush();

        // When
        userService.delete(existingUser.getId());

        // Then
        assertNull(em.find(User.class, existingUser.getId()));
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Given
        User user1 = new User(null, "User 1", "user1@example.com");
        User user2 = new User(null, "User 2", "user2@example.com");
        em.persist(user1);
        em.persist(user2);
        em.flush();

        // When
        List<UserDto> users = userService.getAll();

        // Then
        assertEquals(2, users.size());
        assertThat(users).extracting(UserDto::getName)
                .containsExactlyInAnyOrder("User 1", "User 2");
    }

    @Test
    void checkUserExists_WhenUserExists_ShouldReturnUser() {
        // Given
        User existingUser = new User(null, "Existing", "exist@example.com");
        em.persist(existingUser);
        em.flush();

        // When
        User user = userService.checkUserExists(existingUser.getId());

        // Then
        assertEquals(existingUser.getId(), user.getId());
    }

    @Test
    void checkUserExists_WhenUserNotExists_ShouldThrowException() {
        // When & Then
        assertThrows(UserException.class, () -> userService.checkUserExists(999L));
    }
}
