package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Repository
public class UserStorageImpl implements UserStorage {
    private HashMap<Long, User> userDb = new HashMap<>();
    private Long idCounter = 1L;

    /**
     * Создает пользователя
     *
     * @param user пользователь
     * @return созданного пользователя
     * @throws RuntimeException если email уже используется
     */
    @Override
    public User create(User user) {
        user.setId(idCounter++);
        checkExistEmail(user);
        userDb.put(user.getId(), user);
        return user;
    }

    /**
     * Обновляет пользователя
     *
     * @param user   новые данные пользователя
     * @param userId id обновляемого пользователя
     * @return обновленный пользователь
     * @throws NotFoundException если пользователь не найден
     * @throws RuntimeException  если обновляемый email уже используется
     */
    @Override
    public User update(Long userId, User user) {
        if (userDb.isEmpty()) {
            throw new RuntimeException("В базе данных нет пользователей");
        }

        if (userId == null) {
            checkExistEmail(user);
            Long latestId = userDb.keySet().stream().max(Long::compareTo).get();
            user.setId(latestId);
            userDb.put(latestId, user);
            return userDb.get(latestId);
        } else {
            checkUserExist(userId, "Not found user with id " + userId);
            checkExistEmail(user);
            User userToUpdate = userDb.get(userId);
            userToUpdate.setEmail(user.getEmail());
            userToUpdate.setName(user.getName());
            userDb.put(userId, userToUpdate);
            return userToUpdate;
        }
    }

    /**
     * Удаляет пользователя
     *
     * @param userId идентификатор удаляемого пользователя
     * @throws NotFoundException если пользователь не найден
     */
    @Override
    public void delete(Long userId) {
        checkUserExist(userId, "Not found user with id " + userId);
        userDb.remove(userId);
    }

    /**
     * Отправляет пользователя
     *
     * @param userId идентификатор пользователя
     * @throws NotFoundException если пользователь не найден
     */
    @Override
    public User getUser(Long userId) {
        checkUserExist(userId, "Not found user with id " + userId);
        return userDb.get(userId);
    }

    /**
     * Отправляет список всех пользователей
     *
     * @return List всех пользователей
     */
    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(userDb.values());
    }

    /**
     * Проверяет существование email в базе данных
     *
     * @param user пользователь для проверки
     * @throws RuntimeException если email уже используется
     */
    private void checkExistEmail(User user) {
        if (user.getEmail() != null) {
            boolean emailExists = userDb.values().stream()
                    .filter(existingUser -> !existingUser.getId().equals(user.getId()))  // Исключаем текущего пользователя
                    .map(User::getEmail)
                    .filter(Objects::nonNull)  // Фильтруем null-значения email
                    .anyMatch(email -> email.equals(user.getEmail()));  // Теперь email точно не null

            if (emailExists) {
                throw new RuntimeException("Email already exists");
            }
        }
    }

    /**
     * Проверяет существование user в базе данных
     *
     * @param userId id пользователь для проверки
     * @throws NotFoundException если пользователь не найден
     */
    @Override
    public void checkUserExist(Long userId, String message) {
        if (!userDb.containsKey(userId)) {
            throw new NotFoundException(message);
        }
    }
}

