# My Blog Back App

Java 21 / Spring Framework 6.1.x (без Spring Boot) REST backend блога. Сборка в WAR (`ROOT.war`) для деплоя в стандартный сервлет‑контейнер (Tomcat / Jetty). Используется Spring MVC + Spring Data JDBC + PostgreSQL.

## Стек
- Java 21
- Spring Web MVC 6.1.x
- Spring Data JDBC 3.3.x
- PostgreSQL
- Bean Validation (Hibernate Validator)
- Lombok

## Сборка и запуск
```bash
mvn clean package
```
Результат: `target/ROOT.war`.

Деплой: скопировать `target/ROOT.war` в `$TOMCAT_HOME/webapps/`.
Приложение будет доступно по `http://localhost:8080/api/...` (контекст пустой за счёт имени ROOT).

## Конфигурация БД
Настройки в `src/main/resources/application.properties`:

При старте выполняется `schema.sql` (бин `DataSourceInitializer`) — таблицы создаются автоматически. Скрипт безопасен при повторном выполнении (IF NOT EXISTS).
## Тесты
Запуск:
```bash
mvn test
```
## Лицензия
Internal demo.
