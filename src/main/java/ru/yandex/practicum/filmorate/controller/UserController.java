package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1;
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @PostMapping
    public User createUsers(@RequestBody User user) {
        if (user.getEmail() == null || !user.getEmail().contains("@") || user.getEmail().isBlank()) {
            log.warn("Ошибка валидации: электронная почта пустая или не содержит требующихся символов");
            throw new ValidationException("электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().contains(" ") || user.getLogin().isBlank()) {
            log.warn("Ошибка валидации: неправильная запись пароля");
            throw new ValidationException("логин не может быть пустым и содержать пробелы");
        }

        if (user.getName() == null) {
            user.setName(user.getLogin());
        }

        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Ошибка валидации: неправильно указана дата рождения");
            throw new ValidationException("дата рождения не может быть в будущем");
        }

        user.setId(nextId++);
        users.put(user.getId(), user);
        log.info("Создан пользователь с id - " + user.getId());
        return user;
    }

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        if (!users.containsKey(newUser.getId())) {
            log.warn("Ошибка валидации: текущий пользователь не найден");
            throw new ValidationException("Пользователь с id: " + newUser.getId() + "не найден");
        }

        users.put(newUser.getId(), newUser);
        log.info("Пользователь с id - " + newUser.getId() + "успешно обновлен");
        return newUser;
    }
}
