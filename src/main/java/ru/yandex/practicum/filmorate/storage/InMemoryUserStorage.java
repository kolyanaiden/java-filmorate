package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private final AtomicInteger currentId = new AtomicInteger(1);

    @Override
    public User addUser(User user) {
        user.setId(currentId.getAndIncrement());
        users.put(user.getId(), user);
        log.debug("Пользователь добавлен в хранилище: id={}, login={}", user.getId(), user.getLogin());
        return user;
    }

    @Override
    public User updateUser(User user) {
        users.put(user.getId(), user);
        log.debug("Пользователь обновлен в хранилище: id={}, login={}", user.getId(), user.getLogin());
        return user;
    }

    @Override
    public void deleteUser(Integer id) {
        users.remove(id);
        log.debug("Пользователь удален из хранилища: id={}", id);
    }

    @Override
    public Optional<User> getUserById(Integer id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public boolean existsById(Integer id) {
        return users.containsKey(id);
    }
}