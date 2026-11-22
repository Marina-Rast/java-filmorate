package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.UserDbStorage;


import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    private User makeTestUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName("Test Name");
        user.setBirthday(LocalDate.of(1995, 5, 15));
        return user;
    }

    @Test
    void testCreateAndFindUserById() {
        User created = userStorage.createUsers(makeTestUser("user1@mail.ru", "user1"));

        Optional<User> found = userStorage.findById(created.getId());

        assertThat(found)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getId()).isEqualTo(created.getId());
                    assertThat(user.getEmail()).isEqualTo("user1@mail.ru");
                    assertThat(user.getLogin()).isEqualTo("user1");
                });
    }

    @Test
    void testUpdateUser() {
        User created = userStorage.createUsers(makeTestUser("old@mail.ru", "oldLogin"));
        created.setName("Updated Name");
        created.setEmail("new@mail.ru");

        userStorage.update(created);
        Optional<User> updated = userStorage.findById(created.getId());

        assertThat(updated)
                .isPresent()
                .hasValueSatisfying(user -> {
                    assertThat(user.getName()).isEqualTo("Updated Name");
                    assertThat(user.getEmail()).isEqualTo("new@mail.ru");
                });
    }

    @Test
    void testFindAllUsers() {
        userStorage.createUsers(makeTestUser("a@mail.ru", "loginA"));
        userStorage.createUsers(makeTestUser("b@mail.ru", "loginB"));

        Collection<User> users = userStorage.findAll();

        assertThat(users).hasSize(2);
    }

    @Test
    void testDeleteUser() {
        User created = userStorage.createUsers(makeTestUser("del@mail.ru", "delLogin"));

        userStorage.delete(created.getId());

        assertThat(userStorage.findById(created.getId())).isEmpty();
    }
}
