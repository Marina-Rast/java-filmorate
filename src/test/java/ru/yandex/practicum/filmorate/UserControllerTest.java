package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import static org.junit.jupiter.api.Assertions.*;

public class UserControllerTest {

    @Test
    public void shouldThrowExceptionWhenEmailIsEmpty() {
        UserController controller = new UserController();
        User user = new User();
        user.setEmail("");
        user.setLogin("validLogin");

        assertThrows(ValidationException.class, () -> {
            controller.createUsers(user);
        });
    }

    @Test
    public void shouldThrowExceptionWhenEmailHasNoAtSymbol() {
        UserController controller = new UserController();
        User user = new User();
        user.setEmail("invalidEmail.com");
        user.setLogin("validLogin");

        assertThrows(ValidationException.class, () -> {
            controller.createUsers(user);
        });
    }

    @Test
    public void shouldThrowExceptionWhenLoginIsBlank() {
        UserController controller = new UserController();
        User user = new User();
        user.setEmail("valid@mail.com");
        user.setLogin("   ");

        assertThrows(ValidationException.class, () -> {
            controller.createUsers(user);
        });
    }

    @Test
    public void shouldThrowExceptionWhenBirthdayInFuture() {
        UserController controller = new UserController();
        User user = new User();
        user.setEmail("valid@mail.com");
        user.setLogin("validLogin");
        user.setBirthday(java.time.LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> {
            controller.createUsers(user);
        });
    }

    @Test
    public void shouldCreateUserWhenAllFieldsAreValid() {
        UserController controller = new UserController();
        User user = new User();
        user.setEmail("valid@mail.com");
        user.setLogin("validLogin");
        user.setBirthday(java.time.LocalDate.of(2000, 1, 1));

        User createdUser = controller.createUsers(user);

        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());
        assertEquals("validLogin", createdUser.getLogin());
    }
}