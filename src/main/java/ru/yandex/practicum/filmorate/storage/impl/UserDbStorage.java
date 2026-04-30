package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    public User save(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"user_id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKey().intValue());
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId());
        return getById(user.getId()).orElse(user);
    }

    @Override
    public Optional<User> getById(int id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, id);
        return users.stream().findFirst();
    }

    @Override
    public List<User> getAll() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    @Override
    public void addFriend(int userId, int friendId) {
        // Проверяем, существует ли уже запись
        String checkSql = "SELECT COUNT(*) FROM friendship WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, userId, friendId);

        if (count == null || count == 0) {
            // Создаем новую запись о дружбе (одностороннюю)
            String insertSql = "INSERT INTO friendship (user_id, friend_id, status) VALUES (?, ?, 'CONFIRMED')";
            jdbcTemplate.update(insertSql, userId, friendId);
        }
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        // Удаляем только свою запись о дружбе
        String sql = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public List<User> getFriends(int userId) {
        // Получаем всех, кого пользователь добавил в друзья
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendship f ON u.user_id = f.friend_id " +
                "WHERE f.user_id = ?";
        return jdbcTemplate.query(sql, userRowMapper, userId);
    }

    @Override
    public List<User> getCommonFriends(int userId, int friendId) {
        // Находим пересечение друзей двух пользователей
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendship f1 ON u.user_id = f1.friend_id " +
                "JOIN friendship f2 ON u.user_id = f2.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ?";
        return jdbcTemplate.query(sql, userRowMapper, userId, friendId);
    }
}