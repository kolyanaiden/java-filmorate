package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(Integer userId, Integer friendId) {
        if (userId.equals(friendId)) {
            throw new IllegalArgumentException("Нельзя добавить самого себя в друзья");
        }

        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        User friend = userStorage.getUserById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + friendId + " не найден"));

        user.addFriend(friendId);
        friend.addFriend(userId);

        log.info("Пользователи {} и {} теперь друзья", userId, friendId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        User friend = userStorage.getUserById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + friendId + " не найден"));

        user.removeFriend(friendId);
        friend.removeFriend(userId);

        log.info("Пользователи {} и {} больше не друзья", userId, friendId);
    }

    public List<User> getFriends(Integer userId) {
        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        return user.getFriends().stream()
                .map(friendId -> userStorage.getUserById(friendId)
                        .orElseThrow(() -> new NotFoundException("Друг с id " + friendId + " не найден")))
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Integer userId, Integer otherId) {
        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        User other = userStorage.getUserById(otherId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + otherId + " не найден"));

        Set<Integer> userFriends = user.getFriends();
        Set<Integer> otherFriends = other.getFriends();

        userFriends.retainAll(otherFriends);

        return userFriends.stream()
                .map(friendId -> userStorage.getUserById(friendId)
                        .orElseThrow(() -> new NotFoundException("Друг с id " + friendId + " не найден")))
                .collect(Collectors.toList());
    }

    public User getUserById(Integer id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User addUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Имя пользователя не указано, используем логин: {}", user.getLogin());
        }
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        if (!userStorage.existsById(user.getId())) {
            throw new NotFoundException("Пользователь с id " + user.getId() + " не найден");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.updateUser(user);
    }
}