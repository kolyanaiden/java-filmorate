package ru.yandex.practicum.filmorate.storage.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.MpaRatingStorage;
import ru.yandex.practicum.filmorate.storage.mappers.MpaRatingRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaRatingDbStorage implements MpaRatingStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MpaRatingRowMapper mpaRatingRowMapper;

    @Override
    public List<MpaRating> getAll() {
        String sql = "SELECT * FROM mpa_ratings ORDER BY rating_id";
        return jdbcTemplate.query(sql, mpaRatingRowMapper);
    }

    @Override
    public Optional<MpaRating> getById(int id) {
        String sql = "SELECT * FROM mpa_ratings WHERE rating_id = ?";
        List<MpaRating> ratings = jdbcTemplate.query(sql, mpaRatingRowMapper, id);
        return ratings.stream().findFirst();
    }
}