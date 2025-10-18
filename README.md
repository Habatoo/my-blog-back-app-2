# my-blog-back-app
![Java](https://img.shields.io/badge/Java-17-informational?logo=java)
![Postgres](https://img.shields.io/badge/PostgreSQL-17-informational?logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-compose-blue?logo=docker)

## О проекте

**my-blog-back-app** <br>
Бэкенд многомодульного блог-приложения на Java/Spring (Maven, Tomcat, Postgres, Flyway, Jacoco).
Поддержка миграций, автотестов, интеграции, docker-compose для легкой разработки и деплоя.
---

## Структура проекта
```declarative;
my-blog-back-app/           # ROOT проекта 
├── api/                    # Контроллеры и конфигурация приложения
│ ├── target/backend.war    # Сборка backend для деплоя в Tomcat/Jetty
│ └── db/migrations/        # Миграции Flyway (V1__init_schema.sql)
├── backend-core/           # Core блок с основной бизнес логикой.
├── frontend/               # Исходный код frontend, конечное приложение - сюда копируется build фронта
├── integrationtests /      # Интеграционные тесты по проекту
├── report /                # Pom для генерации отчетеа jacoco в многомодульном проекте
├── documentation/          # Документация, инструкции, примеры миграций и тестирования
│ ├── database.md
│ ├── deploy.md
│ ├── jacoco.md
│ └── faq.md
├── Dockerfilel
├── docker-compose.yml      # Главный файл оркестрации Docker сервисов
├── .env                    # Переменные среды (НЕ храните в репозитории)
├── README.md
```
---
## Применяемые технологии

- **Java 17** (Spring MVC, Spring Data JPA)
- **PostgreSQL 17** (alpine образ)
- **Flyway** — миграции БД
- **Docker/Docker Compose** — весь стек развертывается одной командой
- **Tomcat 9** — контейнер для деплоя .war backend
- **Nginx** — для фронтенда, проксирования статических файлов
- **Jacoco** — для сбора unit/integration coverage ([как смотреть отчёты](./documentation/jacoco.md))
- **Maven** — сборка проекта, выполнение тестов


## Быстрый старт

1. **Подготовка**
```bash
git clone -b feature/module_one_sprint_three_branch https://github.com/Habatoo/my-blog-back-app.git
cd my-blog-back-app
```

2. **Настройка базы Postgres**

- Параметры по умолчанию:  
  `DB_NAME=blog_db`  
  `USER=blog_admin`  
  `PASSWORD=blog_password`  
  (см. `.env` в папке env)

- Миграции хранятся здесь:  
  `api/src/main/resources/db/migrations/V1__init_schema.sql`

3. **Запуск через Docker Compose**
```bash
docker compose up --build
```
- Контейнеры: backend (Tomcat + WAR), frontend (NGINX), база, Flyway миграции.

4. **Сборка и деплой бэкенда вручную**
- Сборка WAR:
```
./mvnw clean package -DskipTests=true -Dmaven.test.skip=true
 ```
- Деплой:
  - Скопируйте файл `api/target/api-1.0-SNAPSHOT.war` в `webapps/ROOT.war` вашего Tomcat.

5. **Запуск/тесты**
  - Юнит и интеграционные тесты:
```bash
./mvnw test           # только тесты
./mvnw verify         # тесты + проверка через Jacoco
 ```

6. **Отчёты Jacoco**
  - Генерация:
```bash
./mvnw jacoco:report
 ```
  - Смотрите HTML-отчёты в  
  `report/target/site/jacoco/index.html`  
  (может отличаться по модулю – см. doc/jacoco.md)

---


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

- [Работа с БД и миграциями Flyway](./documentation/database.md)
- [Руководство по деплою и настройкам](./documentation/deploy.md)
- [Получение отчетов jacoco](./documentation/database.md)
---
