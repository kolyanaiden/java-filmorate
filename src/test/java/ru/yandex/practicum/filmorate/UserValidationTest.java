package ru.yandex.practicum.filmorate.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class UserValidationTest {
    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    @Test
    void shouldNotAddUserWithEmptyEmail() {
        User user = new User();
        user.setEmail("");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void shouldNotAddUserWithEmailWithoutAt() {
        User user = new User();
        user.setEmail("testemail.com");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void shouldNotAddUserWithEmptyLogin() {
        User user = new User();
        user.setEmail("test@email.com");
        user.setLogin("");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void shouldNotAddUserWithLoginContainingSpaces() {
        User user = new User();
        user.setEmail("test@email.com");
        user.setLogin("user login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsEmpty() {
        User user = new User();
        user.setEmail("test@email.com");
        user.setLogin("userlogin");
        user.setName("");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userController.createUser(user);

        assertEquals("userlogin", createdUser.getName());
    }

    @Test
    void shouldNotAddUserWithBirthdayInFuture() {
        User user = new User();
        user.setEmail("test@email.com");
        user.setLogin("login");
        user.setName("Name");
        // Используем дату на один день вперед от текущей
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> userController.createUser(user));
    }

    @Test
    void shouldAddUserWithBirthdayToday() {
        User user = new User();
        user.setEmail("test@email.com");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.now());

        User createdUser = userController.createUser(user);

        assertNotNull(createdUser.getId());
        assertEquals(LocalDate.now(), createdUser.getBirthday());
    }

    @Test
    void shouldAddValidUser() {
        User user = new User();
        user.setEmail("test@email.com");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userController.createUser(user);

        assertNotNull(createdUser.getId());
        assertEquals("test@email.com", createdUser.getEmail());
        assertEquals("userlogin", createdUser.getLogin());
        assertEquals("User Name", createdUser.getName());
    }
}