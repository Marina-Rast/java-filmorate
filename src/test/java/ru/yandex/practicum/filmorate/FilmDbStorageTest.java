package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.MpaDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, MpaDbStorage.class, GenreDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final MpaDbStorage mpaStorage;
    private final GenreDbStorage genreStorage;

    private Film makeTestFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        film.setMpa(new Mpa(1, null));
        film.setGenres(Set.of(new Genre(1, null)));
        return film;
    }

    @Test
    void testAddAndFindFilmById() {
        Film created = filmStorage.addFilm(makeTestFilm("Matrix"));

        Optional<Film> found = filmStorage.findById(created.getId());

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film.getName()).isEqualTo("Matrix");
                    assertThat(film.getMpa().getName()).isEqualTo("G");
                    assertThat(film.getGenres()).extracting(Genre::getName).containsExactly("Комедия");
                });
    }

    @Test
    void testUpdateFilm() {
        Film created = filmStorage.addFilm(makeTestFilm("Old Title"));
        created.setName("New Title");
        created.setDuration(150);

        filmStorage.update(created);
        Optional<Film> updated = filmStorage.findById(created.getId());

        assertThat(updated)
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film.getName()).isEqualTo("New Title");
                    assertThat(film.getDuration()).isEqualTo(150);
                });
    }

    @Test
    void testFindAllFilms() {
        filmStorage.addFilm(makeTestFilm("Film 1"));
        filmStorage.addFilm(makeTestFilm("Film 2"));

        Collection<Film> films = filmStorage.findAll();

        assertThat(films).hasSize(2);
    }

    @Test
    void testDeleteFilm() {
        Film created = filmStorage.addFilm(makeTestFilm("To Delete"));

        filmStorage.delete(created.getId());

        assertThat(filmStorage.findById(created.getId())).isEmpty();
    }

    @Test
    void testMpaAndGenres() {
        assertThat(mpaStorage.findAll()).hasSize(5);
        assertThat(mpaStorage.findById(1)).isPresent();

        assertThat(genreStorage.findAll()).hasSize(6);
        assertThat(genreStorage.findById(1)).isPresent();
    }
}
