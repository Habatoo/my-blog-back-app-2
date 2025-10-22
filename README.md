# my-blog-back-app
![Java](https://img.shields.io/badge/Java-17-informational?logo=java)
![Postgres](https://img.shields.io/badge/PostgreSQL-17-informational?logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-compose-blue?logo=docker)

## О проекте

**my-blog-back-app** <br>
Бэкенд многомодульного блог-приложения на Java/Spring Boot (Gradle, Postgres, Flyway, Jacoco).
Поддержка миграций, автотестов, интеграции, docker-compose для легкой разработки и деплоя.
---

## Структура проекта

---
## Применяемые технологии

- **Java 17** (Spring Boot, Spring Data JPA)
- **PostgreSQL 17** (alpine образ)
- **Flyway** — миграции БД
- **Docker/Docker Compose** — весь стек развертывается одной командой
- **Nginx** — для фронтенда, проксирования статических файлов
- **Jacoco** — для сбора unit/integration coverage ([как смотреть отчёты](./documentation/jacoco.md))
- **Gradle** — сборка проекта, выполнение тестов


## Быстрый старт

1. **Подготовка**


2. **Настройка базы Postgres**


3. **Запуск через Docker Compose**


4. **Сборка и деплой бэкенда вручную**


5. **Запуск/тесты**


6. **Отчёты Jacoco**

---


## Доступы и взаимодействие сервисов



## Более расширенные инструкции

- [Работа с БД и миграциями Flyway](./documentation/database.md)
- [Руководство по деплою и настройкам](./documentation/deploy.md)
- [Получение отчетов jacoco](./documentation/database.md)
---
