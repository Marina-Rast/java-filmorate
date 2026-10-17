package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class FilmControllerTest {

    @Test
    public void shouldThrowExceptionWhenNameIsEmpty() {
        FilmController controller = new FilmController();
        Film film = new Film();
        film.setName("");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> {
            controller.addFilm(film);
        });
    }

    @Test
    public void shouldThrowExceptionWhenDescriptionTooLong() {
        FilmController controller = new FilmController();
        Film film = new Film();
        film.setName("Valid Name");
        film.setDescription("A".repeat(201));
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> {
            controller.addFilm(film);
        });
    }

    @Test
    public void shouldThrowExceptionWhenReleaseDateIsBefore1895() {
        FilmController controller = new FilmController();
        Film film = new Film();
        film.setName("Valid Name");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> {
            controller.addFilm(film);
        });
    }

    @Test
    public void shouldThrowExceptionWhenDurationIsZeroOrNegative() {
        FilmController controller = new FilmController();
        Film film = new Film();
        film.setName("Valid Name");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(0);

        assertThrows(ValidationException.class, () -> {
            controller.addFilm(film);
        });
    }

    @Test
    public void shouldAddFilmWhenAllFieldsAreValid() {
        FilmController controller = new FilmController();
        Film film = new Film();
        film.setName("Valid Name");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Film createdFilm = controller.addFilm(film);

        assertNotNull(createdFilm);
        assertNotNull(createdFilm.getId());
        assertEquals("Valid Name", createdFilm.getName());
    }
}