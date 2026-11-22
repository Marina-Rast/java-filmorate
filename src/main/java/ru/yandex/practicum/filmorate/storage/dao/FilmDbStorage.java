package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@Qualifier("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbc;

    private final RowMapper<Film> filmRowMapper = (rs, rowNum) -> {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        Date releaseDate = rs.getDate("release_date");
        if (releaseDate != null) {
            film.setReleaseDate(releaseDate.toLocalDate());
        }
        film.setDuration(rs.getInt("duration"));

        int mpaId = rs.getInt("mpa_id");
        if (!rs.wasNull()) {
            film.setMpa(new Mpa(mpaId, rs.getString("mpa_name")));
        }
        return film;
    };

    @Override
    public Film addFilm(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null);
            ps.setInt(4, film.getDuration());
            if (film.getMpa() != null && film.getMpa().getId() != null) {
                ps.setInt(5, film.getMpa().getId());
            } else {
                ps.setNull(5, Types.INTEGER);
            }
            return ps;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        saveGenresAndLikes(film);
        return findById(film.getId()).orElse(film);
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        jdbc.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate() != null ? Date.valueOf(film.getReleaseDate()) : null,
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId());
        saveGenresAndLikes(film);
        return findById(film.getId()).orElse(film);
    }

    @Override
    public Collection<Film> findAll() {
        String sql = "SELECT f.*, m.name AS mpa_name FROM films f LEFT JOIN mpa_ratings m ON f.mpa_id = m.id";
        List<Film> films = jdbc.query(sql, filmRowMapper);
        loadGenresAndLikes(films);
        return films;
    }

    @Override
    public void delete(long id) {
        jdbc.update("DELETE FROM films WHERE id = ?", id);
    }

    @Override
    public Optional<Film> findById(long id) {
        String sql = "SELECT f.*, m.name AS mpa_name FROM films f LEFT JOIN mpa_ratings m ON f.mpa_id = m.id WHERE f.id = ?";
        Optional<Film> film = jdbc.query(sql, filmRowMapper, id).stream().findFirst();
        film.ifPresent(this::loadGenresAndLikes);
        return film;
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = "SELECT f.*, m.name AS mpa_name " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_id = m.id " +
                "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                "GROUP BY f.id, m.name " +
                "ORDER BY COUNT(fl.user_id) DESC " +
                "LIMIT ?";
        List<Film> films = jdbc.query(sql, filmRowMapper, count);
        loadGenresAndLikes(films);
        return films;
    }

    private void loadGenresAndLikes(Film film) {
        loadGenresAndLikes(List.of(film));
    }

    private void loadGenresAndLikes(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }
        Map<Long, Film> filmMap = films.stream()
                .collect(Collectors.toMap(Film::getId, Function.identity()));

        String placeholders = filmMap.keySet().stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));
        Object[] ids = filmMap.keySet().toArray();

        String genresSql = "SELECT fg.film_id, g.id, g.name " +
                "FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id IN (" + placeholders + ") " +
                "ORDER BY g.id";
        jdbc.query(genresSql, rs -> {
            Film film = filmMap.get(rs.getLong("film_id"));
            if (film != null) {
                film.getGenres().add(new Genre(rs.getInt("id"), rs.getString("name")));
            }
        }, ids);

        String likesSql = "SELECT film_id, user_id " +
                "FROM film_likes " +
                "WHERE film_id IN (" + placeholders + ")";
        jdbc.query(likesSql, rs -> {
            Film film = filmMap.get(rs.getLong("film_id"));
            if (film != null) {
                film.getLikes().add(rs.getLong("user_id"));
            }
        }, ids);
    }

    private void saveGenresAndLikes(Film film) {
        jdbc.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                jdbc.update("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                        film.getId(), genre.getId());
            }
        }

        jdbc.update("DELETE FROM film_likes WHERE film_id = ?", film.getId());
        if (film.getLikes() != null) {
            for (Long userId : film.getLikes()) {
                jdbc.update("INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)",
                        film.getId(), userId);
            }
        }
    }
}
