## Быстрый старт

0. **Секреты**
   <br> Обеспечить наличие секртетов в `./env/.env` 
   <br> Содержимое файла `.env`
```text
SPRING_DATASOURCE_URL=jdbc:postgresql://blog_db_con:5432/blog_db
SPRING_DATASOURCE_USERNAME=blog_admin
SPRING_DATASOURCE_PASSWORD=blog_password
POSTGRES_DB=blog_db
POSTGRES_USER=blog_admin
POSTGRES_PASSWORD=blog_password
FLYWAY_URL=jdbc:postgresql://blog_db_con:5432/blog_db
FLYWAY_USER=blog_admin
FLYWAY_PASSWORD=blog_password
```

1. **Собрать фронтенд**
   <br> Соберите клиентское приложение обычным способом; положите production-билд в папку `./frontend`.

2. **Собрать backend**


3. **Подготовить миграции Flyway и настроить переменные БД**


4. **Собрать и запустить Docker-кластер**

После успешной сборки образов и запуск с помощью
```bash
docker compose build
docker compose up -d
```
- Проверка запущенных контейнеров
```bash
docker compose ps
```
покажет статус сервисов. Убедитесь, что контейнеры my-blog-backend, my-blog-front-app, db, flyway успешно работают.

- Управление
  Для остановки всех сервисов:
```bash
docker compose down
```
  Для пересборки всех сервисов:
```bash
docker compose up -d --build
```

- Логирование
Для просмотра логов используйте:
```bash
docker compose logs -f my-blog-backend
docker compose logs -f my-blog-front-app
docker compose logs -f flyway
```
Это поможет убедиться, что сервисы запустились без ошибок.

- Проверка БД
Чтобы проверить базу данных в контейнере PostgreSQL и структуру таблиц, сделайте следующее:
Подключитесь к контейнеру с базой данных командой:

```bash
docker exec -it blog_db_con psql -U blog_admin -d blog_db
```
где blog_db_con — имя вашего контейнера, blog_admin — пользователь БД, blog_db — база данных.

После подключения в интерактивной оболочке psql выполните команду для просмотра всех таблиц:
```sql
\dt
```
Чтобы посмотреть структуру конкретной таблицы (например, post), выполните:
```sql
\d+ post
```
Для выхода из psql нажмите
```sql
\q 
```
и Enter.

---
