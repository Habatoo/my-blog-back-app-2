-- Очистка данных
DELETE FROM comment;
DELETE FROM post_tag;
DELETE FROM post;
DELETE FROM tag;

-- Сброс автоинкрементной последовательности id
ALTER SEQUENCE comment_id_seq RESTART WITH 1;
ALTER SEQUENCE post_id_seq RESTART WITH 1;
ALTER SEQUENCE tag_id_seq RESTART WITH 1;

---- Вставка тегов (с ручным id)
--INSERT INTO tag (id, name) VALUES (1, 'java');
--INSERT INTO tag (id, name) VALUES (2, 'spring');
--INSERT INTO tag (id, name) VALUES (3, 'programming');
--INSERT INTO tag (id, name) VALUES (4, 'database');
--INSERT INTO tag (id, name) VALUES (5, 'tutorial');
--
---- Вставка постов (с ручным id)
--INSERT INTO post (id, title, text, likes_count, comments_count, created_at, updated_at) VALUES
--(1, 'Мой первый пост о Java', 'Изучаю Java и Spring Framework. Очень интересная технология!', 5, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
--(2, 'Spring Boot преимущества', 'Spring Boot упрощает разработку приложений. Автоконфигурация - это круто!', 8, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
--(3, 'Работа с базами данных', 'Рассказываю о основах работы с PostgreSQL и H2 в Spring приложениях. И еще добиваем число символов больше 128 и проверяем разные символы 1234567890!"№;%:?*()_/{}[]', 3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
--(4, 'Советы по программированию', 'Несколько полезных советов для начинающих разработчиков.', 2, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
--(5, 'Без тегов пример', 'Этот пост создан без тегов для демонстрации.', 1, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
--
---- Связывание постов с тегами (теперь id четко заданы)
--INSERT INTO post_tag (post_id, tag_id) VALUES (1, 1);
--INSERT INTO post_tag (post_id, tag_id) VALUES (1, 2);
--INSERT INTO post_tag (post_id, tag_id) VALUES (1, 3);
--
--INSERT INTO post_tag (post_id, tag_id) VALUES (2, 1);
--INSERT INTO post_tag (post_id, tag_id) VALUES (2, 2);
--
--INSERT INTO post_tag (post_id, tag_id) VALUES (3, 1);
--INSERT INTO post_tag (post_id, tag_id) VALUES (3, 4);
--INSERT INTO post_tag (post_id, tag_id) VALUES (3, 5);
--
--INSERT INTO post_tag (post_id, tag_id) VALUES (4, 3);
--INSERT INTO post_tag (post_id, tag_id) VALUES (4, 5);
--
---- Вставка комментариев (с ручным id)
--INSERT INTO comment (id, post_id, text, created_at, updated_at) VALUES
--(1, 1, 'Отличный первый пост! Удачи в изучении Java!', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
--(2, 1, 'Spring Framework действительно мощный инструмент.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
--(3, 2, 'Spring Boot экономит так много времени!', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
--(4, 2, 'Можно пример настройки автоконфигурации?', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
--(5, 2, 'Спасибо за полезную информацию!', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
--(6, 3, 'Хорошее объяснение основ работы с БД.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);