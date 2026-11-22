INSERT INTO mpa_ratings (id, name, description)
SELECT * FROM (VALUES
    (1, 'G', 'У фильма нет возрастных ограничений'),
    (2, 'PG', 'Детям рекомендуется смотреть фильм с родителями'),
    (3, 'PG-13', 'Детям до 13 лет просмотр не желателен'),
    (4, 'R', 'Лицам до 17 лет просматривать фильм можно только в присутствии взрослого'),
    (5, 'NC-17', 'Лицам до 18 лет просмотр запрещён')
) AS val(id, name, description)
WHERE NOT EXISTS (SELECT 1 FROM mpa_ratings WHERE id = val.id);

INSERT INTO genres (id, name)
SELECT * FROM (VALUES
    (1, 'Комедия'),
    (2, 'Драма'),
    (3, 'Мультфильм'),
    (4, 'Триллер'),
    (5, 'Документальный'),
    (6, 'Боевик')
) AS val(id, name)
WHERE NOT EXISTS (SELECT 1 FROM genres WHERE id = val.id);
