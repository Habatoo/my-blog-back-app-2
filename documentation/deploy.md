## Быстрый старт

1. **Собрать фронтенд**
    - Соберите клиентское приложение обычным способом; положите production-билд в папку `./frontend`.

2. **Собрать backend**
    - В root проекта выполните:
      ```
      mvnw.cmd clean install
      ```
      или
         ```
      mvnw clean install
      ```
   После сборки гарантируется появление файлов
    - `./api/target/api-1.0-SNAPSHOT.war`.
    - `./backend-core/target/backend-core-1.0-SNAPSHOT.jar`.
   Файл backend-1.0-SNAPSHOT.war содержит все нужные зависимости из модуля backend-core

3. **Подготовить миграции Flyway и настроить переменные БД**
    - Миграции должны лежать в `./api/src/main/resources/db/migrations/`
    - .env файл — в нем заданы параметры БД.

4. **Собрать и запустить Docker-кластер**
    - Указать путь до .m2 
      `echo 'export M2_PATH=/home/lsy/.m2' >> ~/.bashrc`
      `source ~/.bashrc`
      Либо
      `sudo M2_PATH=$M2_PATH docker compose build`


После успешной сборки образов с помощью
```bash
sudo M2_PATH=$M2_PATH docker compose build
````
следующий шаг — запустить сервисы, описанные в docker-compose.yml. Вы уже сделали это командой:
```bash
sudo M2_PATH=$M2_PATH docker compose up -d
```
- Проверка запущенных контейнеров
Команда:
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
sudo M2_PATH=$M2_PATH docker compose up -d --build
```

- Доступ к приложениям
    Фронтенд: откройте браузер по адресу http://localhost или http://127.0.0.1 (порт 80), он проксируется в nginx контейнер my-blog-front-app.
    Бэкенд: http://localhost:8080 — приложение на Tomcat с вашим backend.

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