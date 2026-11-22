package ru.yandex.practicum.filmorate.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryUserStorage implements UserStorage {
    private static final Logger log = LoggerFactory.getLogger(InMemoryUserStorage.class);
    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1;

    @Override
    public User createUsers(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        log.info("Создан пользователь с id - " + user.getId());
        return user;
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User update(User newUser) {
        users.put(newUser.getId(), newUser);
        log.info("Пользователь с id - " + newUser.getId() + "успешно обновлен");
        return newUser;
    }

    @Override
    public void delete(long id) {
        users.remove(id);
    }

    @Override
    public Optional<User> findById(long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void addFriend(long userId, long friendId) {
        User user = users.get(userId);
        if (user != null) {
            user.getFriends().add(friendId);
        }
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        User user = users.get(userId);
        if (user != null) {
            user.getFriends().remove(friendId);
        }
    }

    @Override
    public List<User> getFriends(long userId) {
        User user = users.get(userId);
        if (user == null) {
            return List.of();
        }
        return user.getFriends().stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(long userId, long otherId) {
        User user = users.get(userId);
        User other = users.get(otherId);
        if (user == null || other == null) {
            return List.of();
        }
        return user.getFriends().stream()
                .filter(other.getFriends()::contains)
                .map(users::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
