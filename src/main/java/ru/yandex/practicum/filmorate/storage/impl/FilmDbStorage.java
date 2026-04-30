package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;
    private final GenreRowMapper genreRowMapper;

    @Override
    public Film save(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"film_id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKey().intValue());

        // Сохраняем жанры
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String genreSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(genreSql, film.getId(), genre.getId());
            }
        }

        return getById(film.getId()).orElse(film);
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? WHERE film_id = ?";

        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        // Обновляем жанры
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String genreSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(genreSql, film.getId(), genre.getId());
            }
        }

        return getById(film.getId()).orElse(film);
    }

    @Override
    public Optional<Film> getById(int id) {
        String sql = "SELECT f.*, m.code FROM films f " +
                "JOIN mpa_ratings m ON f.mpa_rating_id = m.rating_id " +
                "WHERE f.film_id = ?";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, id);

        if (films.isEmpty()) {
            return Optional.empty();
        }

        Film film = films.get(0);
        film.setGenres(getFilmGenres(id));
        film.setLikes(getFilmLikes(id));

        return Optional.of(film);
    }

    @Override
    public List<Film> getAll() {
        String sql = "SELECT f.*, m.code FROM films f " +
                "JOIN mpa_ratings m ON f.mpa_rating_id = m.rating_id";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper);

        for (Film film : films) {
            film.setGenres(getFilmGenres(film.getId()));
            film.setLikes(getFilmLikes(film.getId()));
        }

        return films;
    }

    @Override
    public void addLike(int filmId, int userId) {
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getPopular(int count) {
        String sql = "SELECT f.*, m.code, COUNT(fl.user_id) as likes_count " +
                "FROM films f " +
                "JOIN mpa_ratings m ON f.mpa_rating_id = m.rating_id " +
                "LEFT JOIN film_likes fl ON f.film_id = fl.film_id " +
                "GROUP BY f.film_id, m.code " +
                "ORDER BY likes_count DESC " +
                "LIMIT ?";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, count);

        for (Film film : films) {
            film.setGenres(getFilmGenres(film.getId()));
            film.setLikes(getFilmLikes(film.getId()));
        }

        return films;
    }

    private Set<Genre> getFilmGenres(int filmId) {
        String sql = "SELECT g.* FROM genres g " +
                "JOIN film_genres fg ON g.genre_id = fg.genre_id " +
                "WHERE fg.film_id = ? " +
                "ORDER BY fg.film_id, fg.genre_id"; // Оставляем порядок как в запросе
        List<Genre> genres = jdbcTemplate.query(sql, genreRowMapper, filmId);
        return new LinkedHashSet<>(genres); // Используем LinkedHashSet для сохранения порядка
    }

    private Set<Integer> getFilmLikes(int filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        List<Integer> likes = jdbcTemplate.queryForList(sql, Integer.class, filmId);
        return new HashSet<>(likes);
    }
}