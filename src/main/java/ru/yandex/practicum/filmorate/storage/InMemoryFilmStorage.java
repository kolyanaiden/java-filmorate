package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private final AtomicInteger currentId = new AtomicInteger(1);

    @Override
    public Film addFilm(Film film) {
        film.setId(currentId.getAndIncrement());
        films.put(film.getId(), film);
        log.debug("Фильм добавлен в хранилище: id={}, name={}", film.getId(), film.getName());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        films.put(film.getId(), film);
        log.debug("Фильм обновлен в хранилище: id={}, name={}", film.getId(), film.getName());
        return film;
    }

    @Override
    public void deleteFilm(Integer id) {
        films.remove(id);
        log.debug("Фильм удален из хранилища: id={}", id);
    }

    @Override
    public Optional<Film> getFilmById(Integer id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public boolean existsById(Integer id) {
        return films.containsKey(id);
    }
}