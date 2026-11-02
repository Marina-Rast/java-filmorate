package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Film addFilm(Film film);
    Film update(Film film);
    Collection<Film> findAll();
    void delete(long id);
    Optional<Film> findById(long id);
}
