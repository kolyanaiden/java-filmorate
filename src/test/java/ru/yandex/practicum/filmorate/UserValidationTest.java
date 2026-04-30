package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("ci")
public class UserValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldNotAddUserWithEmptyEmail() throws Exception {
        User user = new User();
        user.setEmail("");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotAddUserWithEmailWithoutAt() throws Exception {
        User user = new User();
        user.setEmail("testemail.com");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotAddUserWithEmptyLogin() throws Exception {
        User user = new User();
        user.setEmail("test@email.com");
        user.setLogin("");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotAddUserWithLoginContainingSpaces() throws Exception {
        User user = new User();
        user.setEmail("test@email.com");
        user.setLogin("user login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotAddUserWithBirthdayInFuture() throws Exception {
        User user = new User();
        user.setEmail("test@email.com");
        user.setLogin("login");
        user.setName("Name");
        user.setBirthday(LocalDate.now().plusDays(1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldAddValidUser() throws Exception {
        User user = new User();
        user.setEmail("test@email.com");
        user.setLogin("userlogin");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@email.com"))
                .andExpect(jsonPath("$.login").value("userlogin"))
                .andExpect(jsonPath("$.name").value("User Name"));
    }
}