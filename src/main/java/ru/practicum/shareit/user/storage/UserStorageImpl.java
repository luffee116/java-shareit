package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exceptions.NotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

        if (userDb.values()
                .stream()
                .map(User::getEmail)
                .anyMatch(email -> email.equals(user.getEmail()))) {
            throw new RuntimeException("Email already exists");
        }

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
    public User update(String userId, User user) {
        if (userId.equals("null")) {
            checkExistEmail(user);
            Long latestId = userDb.keySet().stream().max(Long::compareTo).get();
            user.setId(latestId);
            userDb.put(latestId, user);
            return userDb.get(latestId);
        } else {
            if (!userDb.containsKey(Long.parseLong(userId))) {
                throw new NotFoundException("User not found");
            }
            checkExistEmail(user);
            User userToUpdate = userDb.get(Long.parseLong(userId));
            userToUpdate.setEmail(user.getEmail());
            userToUpdate.setName(user.getName());
            userDb.put(Long.parseLong(userId), userToUpdate);
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
        if (!userDb.containsKey(userId)) {
            throw new NotFoundException("User not found");
        }
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
        if (!userDb.containsKey(userId)) {
            throw new NotFoundException("User not found");
        }
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
        if (user.getName() != null) {
            boolean exist = userDb.values().stream().map(User::getEmail).anyMatch(email -> email.equals(user.getEmail()));
            if (exist) {
                throw new RuntimeException("Email already exists");
            }
        }
    }
}
