package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public void addLike(Integer filmId, Integer userId) {
        Film film = filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + filmId + " не найден"));

        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        film.addLike(userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        Film film = filmStorage.getFilmById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + filmId + " не найден"));

        User user = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        film.removeLike(userId);
        log.info("Пользователь {} удалил лайк у фильма {}", userId, filmId);
    }

    public List<Film> getPopularFilms(Integer count) {
        int limit = count != null ? count : 10;

        return filmStorage.getAllFilms().stream()
                .sorted(Comparator.comparingInt(Film::getLikesCount).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Film getFilmById(Integer id) {
        return filmStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        if (!filmStorage.existsById(film.getId())) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }
        return filmStorage.updateFilm(film);
    }
}