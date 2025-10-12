# my-blog-back-app
![Java](https://img.shields.io/badge/Java-17-informational?logo=java)
![Postgres](https://img.shields.io/badge/PostgreSQL-17-informational?logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-compose-blue?logo=docker)

## О проекте

**my-blog-back-app** — это учебный блоговый проект, реализованный на базе Java (Spring), PostgreSQL, Docker, Tomcat и современных frontend-технологий. Цель — продемонстрировать микросервисное развёртывание с помощью Docker, централизованные миграции Flyway и модульное покрытие unit-тестами с Jacoco.

---

## Структура проекта
```declarative;
├── backend/ # Исходный код и ресурсы backend (Java, Spring)
├── frontend/ # Исходный код frontend, конечное приложение - сюда копируется build фронта
├── target/ROOT.war # Сборка backend для деплоя в Tomcat
├── documentation/ # Документация, инструкции, примеры миграций и тестирования
│ ├── database.md
│ ├── flyway-migrations.md
│ ├── deploy.md
│ ├── jacoco.md
│ └── faq.md
├── docker-compose.yml # Главный файл оркестрации Docker сервисов
├── .env # Переменные среды (НЕ храните в репозитории)
├── README.md
```
---
## Применяемые технологии

- **Java 17** (Spring MVC, Spring Data JPA)
- **PostgreSQL 17** (alpine образ)
- **Flyway** — миграции БД (описано в [documentation/flyway-migrations.md](./documentation/flyway-migrations.md))
- **Docker/Docker Compose** — весь стек развертывается одной командой
- **Tomcat 9** — контейнер для деплоя .war backend
- **Nginx** — для фронтенда, проксирования статических файлов
- **Jacoco** — для сбора unit/integration coverage ([как смотреть отчёты](./documentation/jacoco.md))
- **Maven** — сборка проекта, выполнение тестов

## Доступы и взаимодействие сервисов

- **Frontend:**  
  http://localhost/  
  (отправляет запросы на API по http://localhost:8080/)
- **Backend:**  
  http://localhost:8080/  
  (автоматически подключается к сервису db и применяет миграции при первом запуске через Flyway)
- **База данных:**  
  `blog_db_con` (Postgres)  
  — видна внутри клстера по имени, снаружи доступен порт 5432
---

## Более расширенные инструкции

- [Работа с БД и миграциями Flyway](./documentation/flyway-migrations.md)
- [Руководство по деплою и настройкам](./documentation/deploy.md)
- [Удаление базы, пользователя, схемы вручную (Postgres)](./documentation/database.md)
---
