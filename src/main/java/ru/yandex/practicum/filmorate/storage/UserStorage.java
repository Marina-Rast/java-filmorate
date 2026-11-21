package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {

    User createUsers(User user);

    User update(User user);

    Collection<User> findAll();

    void delete(long id);

    Optional<User> findById(long id);
}
