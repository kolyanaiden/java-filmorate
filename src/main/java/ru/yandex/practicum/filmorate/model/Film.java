package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.annotation.ValidReleaseDate;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
public class Film {
    private Integer id;

    @NotBlank(message = "Название фильма не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    @NotNull(message = "Дата релиза не может быть null")
    @ValidReleaseDate(message = "Дата релиза не может быть раньше 28 декабря 1895 года")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private Integer duration;

    private MpaRating mpa;

    private Set<Genre> genres = new LinkedHashSet<>(); // LinkedHashSet для сохранения порядка

    private Set<Integer> likes = new LinkedHashSet<>();
}