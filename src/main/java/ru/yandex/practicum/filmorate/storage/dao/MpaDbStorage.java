package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor

public class MpaDbStorage {
    private final JdbcTemplate jdbc;
    private final RowMapper<Mpa> mpaRowMapper = (rs,rowNum) -> {
        return new Mpa(rs.getInt("id"), rs.getString("name"));
    };

    public List<Mpa> findAll() {
        String sql = "SELECT id, name FROM mpa_ratings ORDER BY id";
        return jdbc.query(sql, mpaRowMapper);
    }

    public Optional<Mpa> findById(int id) {
        String sql = "SELECT id, name FROM mpa_ratings WHERE id = ?";
        return jdbc.query(sql,mpaRowMapper,id).stream().findFirst();
    }
}
