package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.MpaDbStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaDbStorage mpaDbStorage;
    private final GenreDbStorage genreDbStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       MpaDbStorage mpaDbStorage,
                       GenreDbStorage genreDbStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaDbStorage = mpaDbStorage;
        this.genreDbStorage = genreDbStorage;
    }

    public void addLike(long filmId, long userId) {
        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Такого фильма нет"));

        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        film.getLikes().add(userId);
        filmStorage.update(film);
    }

    public void removeLike(long filmId, long userId) {
        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Такого фильма нет"));

        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        film.getLikes().remove(userId);
        filmStorage.update(film);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    public Film addFilm(Film film) {
        validateMpaAndGenres(film);
        return filmStorage.addFilm(film);
    }

    public Film getFilmById(long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
    }

    public Film update(Film newFilm) {
        Film oldFilm = filmStorage.findById(newFilm.getId())
                .orElseThrow(() -> new NotFoundException("Фильм не найден"));
        validateMpaAndGenres(newFilm);
        if (newFilm.getLikes() == null || newFilm.getLikes().isEmpty()) {
            newFilm.setLikes(oldFilm.getLikes());
        }

        if (newFilm.getGenres() == null || newFilm.getGenres().isEmpty()) {
            newFilm.setGenres(oldFilm.getGenres());
        }

        if (newFilm.getMpa() == null) {
            newFilm.setMpa(oldFilm.getMpa());
        }

        return filmStorage.update(newFilm);
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public void delete(long id) {
        if (filmStorage.findById(id).isEmpty()) {
            throw new NotFoundException("Фильм не найден");
        }
        filmStorage.delete(id);
    }

    private void validateMpaAndGenres(Film film) {
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            mpaDbStorage.findById(film.getMpa().getId())
                    .orElseThrow(() -> new NotFoundException("Рейтинг MPA с id = " + film.getMpa().getId() + " не найден"));
        }
        if (film.getGenres() != null) {
            for (var genre : film.getGenres()) {
                genreDbStorage.findById(genre.getId())
                        .orElseThrow(() -> new NotFoundException("Жанр с id = " + genre.getId() + " не найден"));
            }
        }
    }
}
