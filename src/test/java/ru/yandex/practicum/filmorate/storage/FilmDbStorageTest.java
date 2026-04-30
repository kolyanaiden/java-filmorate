package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.impl.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({FilmDbStorage.class, FilmRowMapper.class, GenreRowMapper.class})
@ActiveProfiles("ci")
class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmStorage;

    @Test
    public void testSaveAndFindFilmById() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film.setMpa(mpa);

        Film savedFilm = filmStorage.save(film);

        Optional<Film> filmOptional = filmStorage.getById(savedFilm.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(f -> {
                    assertThat(f).hasFieldOrPropertyWithValue("id", savedFilm.getId());
                    assertThat(f).hasFieldOrPropertyWithValue("name", "Test Film");
                    assertThat(f.getMpa()).hasFieldOrPropertyWithValue("id", 1);
                });
    }

    @Test
    public void testUpdateFilm() {
        Film film = new Film();
        film.setName("Original Film");
        film.setDescription("Original Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        film.setMpa(mpa);

        Film savedFilm = filmStorage.save(film);

        savedFilm.setName("Updated Film");
        savedFilm.setDuration(150);

        filmStorage.update(savedFilm);

        Optional<Film> updatedFilm = filmStorage.getById(savedFilm.getId());

        assertThat(updatedFilm)
                .isPresent()
                .hasValueSatisfying(f -> {
                    assertThat(f).hasFieldOrPropertyWithValue("name", "Updated Film");
                    assertThat(f).hasFieldOrPropertyWithValue("duration", 150);
                });
    }
}