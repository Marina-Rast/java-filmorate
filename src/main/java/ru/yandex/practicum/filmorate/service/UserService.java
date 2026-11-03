package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Service
public class UserService {
    private final UserStorage userStorage;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getFriends(long userId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<User> result = new ArrayList<>();
        for (Long friendId : user.getFriends()) {
            userStorage.findById(friendId).ifPresent(result::add);
        }
        return result;
    }

    public void addFriends(long userId, long friendId) {
        log.info("Пользователь {} добавляет в друзья {}", userId, friendId);
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Такой пользователь не найден"));
        User friend = userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Друг не найден"));
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        userStorage.update(user);
        userStorage.update(friend);
    }

    public void removeFriends(long usersId, long friendId) {
        User user = userStorage.findById(usersId)
                .orElseThrow(() -> new NotFoundException("Такой пользователь не найден"));
        User friend = userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Друг не найден"));
        user.getFriends().remove(friendId);
        friend.getFriends().remove(usersId);

        userStorage.update(user);
        userStorage.update(friend);
    }

    public List<User> getCommonFriends(long usersId, long otherId) {
        User user = userStorage.findById(usersId)
                .orElseThrow(() -> new NotFoundException("Такой пользователь не найден"));
        User other = userStorage.findById(otherId)
                .orElseThrow(() -> new NotFoundException("Друг не найден"));
        Set<Long> commonsId = new HashSet<>(user.getFriends());
        commonsId.retainAll(other.getFriends());
        List<User> result = new ArrayList<>();
        for (Long id : commonsId) {
            Optional<User> maybeFriend = userStorage.findById(id);
            if (maybeFriend.isPresent()) {
                result.add(maybeFriend.get());
            }
        }
        return result;
    }

    public User createUser(User user) {

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        User created = userStorage.createUsers(user);
        log.info("Создан пользователь с id = {}", created.getId());
        return created;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User getUserById(long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    public User update( User newUser) {
        if (userStorage.findById(newUser.getId()).isEmpty()) {
            log.warn("Ошибка: пользователь с id = {} не найден", newUser.getId());
            throw new NotFoundException("Пользователь с id: " + newUser.getId() + " не найден");
        }
        if (newUser.getName() == null || newUser.getName().isBlank()) {
            newUser.setName(newUser.getLogin());
        }
        User updated = userStorage.update(newUser);
        log.info("Обновлён пользователь с id = {} ", updated.getId());
        return updated;
    }

    public void delete( long id) {
        if (userStorage.findById(id).isEmpty()) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        userStorage.delete(id);
        log.info("Удалён пользователь с id = {} ", id);
    }

}
