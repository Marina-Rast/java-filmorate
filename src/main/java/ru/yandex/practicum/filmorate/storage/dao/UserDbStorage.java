package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;

@Repository
@Qualifier("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        Date birthday = rs.getDate("birthday");
        if (birthday != null) {
            user.setBirthday(birthday.toLocalDate());
        }
        return user;
    };

    @Override
    public User createUsers(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder(); // Сюда база вернёт сгенерированный id

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null);
            return ps;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        saveFriends(user);
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday() != null ? Date.valueOf(user.getBirthday()) : null,
                user.getId());
        saveFriends(user);
        return user;
    }

    @Override
    public Collection<User> findAll() {
        List<User> users = jdbcTemplate.query("SELECT * FROM users", userRowMapper);
        users.forEach(this::loadFriends); // Для каждого пользователя подгружаем его друзей
        return users;
    }

    @Override
    public void delete(long id) {
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
    }

    @Override
    public Optional<User> findById(long id) {
        Optional<User> user = jdbcTemplate.query("SELECT * FROM users WHERE id = ?", userRowMapper, id)
                .stream()
                .findFirst();
        user.ifPresent(this::loadFriends);
        return user;
    }

    private void loadFriends(User user) {
        String sql = "SELECT friend_id, status FROM friendships WHERE user_id = ?";
        Map<Long, User.FriendshipStatus> friends = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            friends.put(
                    rs.getLong("friend_id"),
                    User.FriendshipStatus.valueOf(rs.getString("status"))
            );
        }, user.getId());
        user.setFriends(friends);
    }

    private void saveFriends(User user) {
        jdbcTemplate.update("DELETE FROM friendships WHERE user_id = ?", user.getId());
        if (user.getFriends() != null) {
            String sql = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, ?)";
            for (Map.Entry<Long, User.FriendshipStatus> entry : user.getFriends().entrySet()) {
                jdbcTemplate.update(sql, user.getId(), entry.getKey(), entry.getValue().name());
            }
        }
    }
}
