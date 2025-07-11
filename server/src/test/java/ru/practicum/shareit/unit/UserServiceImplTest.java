package ru.practicum.shareit.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.practicum.shareit.exceptions.UserException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserStorage userStorage;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = User.builder()
                .id(1L)
                .name("User")
                .email("user@example.com")
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .name("User")
                .email("user@example.com")
                .build();
    }

    @Test
    void create_WhenEmailIsUnique_ShouldSaveUser() {
        when(userStorage.existsByEmailAndIdNot(anyString(), any()))
                .thenReturn(false);
        when(userStorage.save(any()))
                .thenReturn(user);

        UserDto result = userService.create(userDto);

        assertThat(result.getId()).isEqualTo(userDto.getId());
        assertThat(result.getEmail()).isEqualTo(userDto.getEmail());
        verify(userStorage).save(any());
    }

    @Test
    void create_WhenEmailExists_ShouldThrowException() {
        when(userStorage.existsByEmailAndIdNot(anyString(), any()))
                .thenReturn(true);

        assertThatThrownBy(() -> userService.create(userDto))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("already exists");

        verify(userStorage, never()).save(any());
    }

    @Test
    void update_WhenUserExists_ShouldUpdateFields() {
        when(userStorage.findById(eq(1L)))
                .thenReturn(Optional.of(user));
        when(userStorage.existsByEmailAndIdNot(anyString(), anyLong()))
                .thenReturn(false);

        UserDto updateDto = UserDto.builder()
                .name("Updated")
                .email("updated@example.com")
                .build();

        UserDto result = userService.update(1L, updateDto);

        assertThat(result.getName()).isEqualTo("Updated");
        assertThat(result.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    void update_WhenEmailAlreadyExists_ShouldThrowException() {
        when(userStorage.findById(eq(1L)))
                .thenReturn(Optional.of(user));
        when(userStorage.existsByEmailAndIdNot(anyString(), anyLong()))
                .thenReturn(true);

        UserDto updateDto = UserDto.builder()
                .email("existing@example.com")
                .build();

        assertThatThrownBy(() -> userService.update(1L, updateDto))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void get_WhenUserExists_ShouldReturnUser() {
        when(userStorage.findById(eq(1L)))
                .thenReturn(Optional.of(user));

        UserDto result = userService.get(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void get_WhenUserNotExists_ShouldThrowException() {
        when(userStorage.findById(eq(1L)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.get(1L))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void delete_WhenUserExists_ShouldDeleteUser() {
        when(userStorage.existsById(eq(1L)))
                .thenReturn(true);

        userService.delete(1L);

        verify(userStorage).deleteById(1L);
    }

    @Test
    void delete_WhenUserNotExists_ShouldThrowException() {
        when(userStorage.existsById(eq(1L)))
                .thenReturn(false);

        assertThatThrownBy(() -> userService.delete(1L))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void getAll_ShouldReturnListOfUsers() {
        when(userStorage.findAll())
                .thenReturn(Collections.singletonList(user));

        List<UserDto> result = userService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void checkUserExists_WhenUserExists_ShouldReturnUser() {
        when(userStorage.findById(eq(1L)))
                .thenReturn(Optional.of(user));

        User result = userService.checkUserExists(1L);

        assertThat(result).isEqualTo(user);
    }

    @Test
    void checkUserExists_WhenUserNotExists_ShouldThrowException() {
        when(userStorage.findById(eq(1L)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.checkUserExists(1L))
                .isInstanceOf(UserException.class)
                .hasMessageContaining("not found");
    }
}
