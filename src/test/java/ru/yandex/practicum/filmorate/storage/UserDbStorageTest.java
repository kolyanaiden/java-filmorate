package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.impl.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({UserDbStorage.class, UserRowMapper.class})
@ActiveProfiles("ci")
class UserDbStorageTest {

    @Autowired
    private UserDbStorage userStorage;

    @Test
    public void testSaveAndFindUserById() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("testuser");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User savedUser = userStorage.save(user);

        Optional<User> userOptional = userStorage.getById(savedUser.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u).hasFieldOrPropertyWithValue("id", savedUser.getId());
                    assertThat(u).hasFieldOrPropertyWithValue("email", "test@test.com");
                    assertThat(u).hasFieldOrPropertyWithValue("login", "testuser");
                });
    }

    @Test
    public void testUpdateUser() {
        User user = new User();
        user.setEmail("original@test.com");
        user.setLogin("original");
        user.setName("Original User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User savedUser = userStorage.save(user);

        savedUser.setName("Updated User");
        savedUser.setEmail("updated@test.com");

        userStorage.update(savedUser);

        Optional<User> updatedUser = userStorage.getById(savedUser.getId());

        assertThat(updatedUser)
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u).hasFieldOrPropertyWithValue("name", "Updated User");
                    assertThat(u).hasFieldOrPropertyWithValue("email", "updated@test.com");
                });
    }

    @Test
    public void testFindAllUsers() {
        User user1 = new User();
        user1.setEmail("user1@test.com");
        user1.setLogin("user1");
        user1.setName("User One");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        userStorage.save(user1);

        User user2 = new User();
        user2.setEmail("user2@test.com");
        user2.setLogin("user2");
        user2.setName("User Two");
        user2.setBirthday(LocalDate.of(1995, 5, 5));
        userStorage.save(user2);

        assertThat(userStorage.getAll()).hasSize(2);
    }
}