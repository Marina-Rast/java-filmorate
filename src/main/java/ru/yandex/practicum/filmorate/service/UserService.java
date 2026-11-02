package ru.yandex.practicum.filmorate.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Service
public class UserService {
    private UserStorage userStorage;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getFriends(long userId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        List<User> result = new ArrayList<>();
        for (Long friendId : user.getFriends()) {
            userStorage.findById(friendId).ifPresent(result::add);
        }
        return result;
    }

    public void addFriends(long userId, long friendId) {
        log.info("Пользователь {} добавляет в друзья {}", userId, friendId);
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new RuntimeException("Такой пользователь не найден"));
        User friend = userStorage.findById(friendId)
                .orElseThrow(() -> new RuntimeException("Друг не найден"));
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        userStorage.update(user);
        userStorage.update(friend);
    }

    public void removeFriends(long usersId, long friendId) {
        User user = userStorage.findById(usersId)
                .orElseThrow(() -> new RuntimeException("Такой пользователь не найден"));
        User friend = userStorage.findById(friendId)
                .orElseThrow(() -> new RuntimeException("Друг не найден"));
        user.getFriends().remove(friendId);
        friend.getFriends().remove(usersId);

        userStorage.update(user);
        userStorage.update(friend);
    }

    public List<User> getCommonFriends(long usersId, long otherId) {
        User user = userStorage.findById(usersId)
                .orElseThrow(() -> new RuntimeException("Такой пользователь не найден"));
        User other = userStorage.findById(otherId)
                .orElseThrow(() -> new RuntimeException("Друг не найден"));
        Set<Long> commonsId = user.getFriends();
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
}
