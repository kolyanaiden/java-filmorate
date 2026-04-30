package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.MpaRatingStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmStorage filmStorage;
    private final MpaRatingStorage mpaRatingStorage;
    private final GenreStorage genreStorage;
    private final UserStorage userStorage;

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос на добавление фильма: {}", film);

        // Проверяем существование MPA рейтинга
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            mpaRatingStorage.getById(film.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("MPA рейтинг с id " + film.getMpa().getId() + " не найден"));
        } else {
            throw new ValidationException("MPA рейтинг должен быть указан");
        }

        // Проверяем существование жанров
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                genreStorage.getById(genre.getId())
                        .orElseThrow(() -> new NotFoundException("Жанр с id " + genre.getId() + " не найден"));
            }
        }

        Film savedFilm = filmStorage.save(film);
        log.info("Фильм успешно добавлен с id: {}", savedFilm.getId());
        return savedFilm;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос на обновление фильма: {}", film);

        if (film.getId() == null) {
            log.error("Попытка обновления фильма без id");
            throw new ValidationException("Id фильма должен быть указан");
        }

        filmStorage.getById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id " + film.getId() + " не найден"));

        Film updatedFilm = filmStorage.update(film);
        log.info("Фильм успешно обновлен с id: {}", updatedFilm.getId());
        return updatedFilm;
    }

    @GetMapping
    public List<Film> getAllFilms() {
        log.info("Получен запрос на получение всех фильмов");
        return filmStorage.getAll();
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable int id) {
        log.info("Получен запрос на получение фильма с id: {}", id);
        return filmStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        log.info("Пользователь {} ставит лайк фильму {}", userId, id);
        filmStorage.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        log.info("Пользователь {} удаляет лайк с фильма {}", userId, id);

        // Проверяем существование фильма и пользователя
        filmStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
        userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        filmStorage.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        log.info("Получен запрос на получение {} популярных фильмов", count);
        return filmStorage.getPopular(count);
    }
}