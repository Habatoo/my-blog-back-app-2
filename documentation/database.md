## Настройка базы для администраторов (PostgreSQL)

Для полного удаления и повторного создания БД и пользователя:

```sql;
DROP DATABASE IF EXISTS blog_db;
DROP ROLE IF EXISTS blog_admin;
DROP TABLE IF EXISTS flyway_schema_history CASCADE;
CREATE ROLE blog_admin WITH LOGIN PASSWORD 'blog_password';
CREATE DATABASE blog_db OWNER blog_admin;
GRANT ALL PRIVILEGES ON DATABASE blog_db TO blog_admin;
```

Дополнительно - Выгнать все активные сессии из blog_db
```sql;
DO
$$
DECLARE
r RECORD;
BEGIN
FOR r IN (SELECT pid FROM pg_stat_activity WHERE datname = 'blog_db' AND pid <> pg_backend_pid())
LOOP
EXECUTE 'SELECT pg_terminate_backend(' || r.pid || ');';
END LOOP;
END
$$;
```
(Опционально) Полная очистка публичной схемы — если надо не удалять базу:
   -- Подключиться к базе: \c blog_db
```sql;
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
```