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
    - `./bacend/target/backend-1.0-SNAPSHOT.war`.
    - `./backend-core/target/backend-core-1.0-SNAPSHOT.jar`.
   Файл backend-1.0-SNAPSHOT.war содержит все нужные зависимости из модуля backend-core

3. **Подготовить миграции Flyway и настроить переменные БД**
    - Миграции должны лежать в `./backend/src/main/resources/db/migrations/`
    - .env файл — в нем заданы параметры БД.

4. **Собрать и запустить Docker-кластер**
    - Собрать образы:
      ```
      docker-compose build
      ```
    - Запустить сервисы в фоне:
      ```
      docker-compose up -d
      ```
---