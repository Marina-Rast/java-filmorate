package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage {
    private final JdbcTemplate jdbc;
    private final RowMapper<Genre> genreRowMapper = (rs, rowMapper) -> {
        return new Genre(rs.getInt("id"),rs.getString("name"));
    };

    public List<Genre> findAll() {
        String sql = "SELECT id, name FROM genres ORDER BY id";
        return jdbc.query(sql, genreRowMapper);
    }

    public Optional<Genre> findById(int id) {
        String sql = "SELECT id,name FROM genres WHERE ID = ?";
        return jdbc.query(sql, genreRowMapper,id).stream().findFirst();
    }

    public List<Genre> findByIds(Collection<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        String params = ids.stream()
                .map(id->"?")
                .collect(Collectors.joining(", "));
        String sql = "SELECT id, name FROM genres WHERE id IN (" + params + ")";
        return jdbc.query(sql, genreRowMapper, ids.toArray());
    }
}
