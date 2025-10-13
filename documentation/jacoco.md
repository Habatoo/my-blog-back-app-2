## Работа с Jacoco — покрытие и отчёты

- Покрытие считается для модулей:
  - backend
  - backend-core

- После выполнения Maven команды:
`mvn clean test jacoco:report`
в модуле report по адресу `./report/target/site/jacoco-aggregate/` будет располгаттсья файл отчета `index.html`
Откройте этот файл в браузере для просмотра уровня покрытия тестами.
---