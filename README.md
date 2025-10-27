# My Blog Backend Application (Sprint 4)

Бэкенд приложения-блога, переписанный с использованием Spring Boot 3.5.6 и Gradle.

## Технологии

- **Java 21**
- **Spring Boot 3.5.6**
  - Spring Boot Starter Web
  - Spring Boot Starter Data JDBC
  - Spring Boot Starter Validation
  - Spring Boot Starter Test
- **Gradle** (система сборки)
- **PostgreSQL** (production database)
- **H2** (test database)
- **Lombok**
- **JUnit 5**
- **Mockito**
- **AssertJ**

## Изменения по сравнению со Sprint 3

1. **Система сборки**: Maven → Gradle
2. **Упаковка**: WAR → Executable JAR
3. **Сервлет-контейнер**: Внешний Tomcat → Встроенный Tomcat
4. **Конфигурация**: Упрощена за счёт Spring Boot Auto-Configuration
5. **Тесты**: Переписаны с использованием Spring Boot Test и кеширования контекстов


### PostgreSQL (Production)
Создайте базу данных:

```sql
CREATE DATABASE blog_db;
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE blog_db TO postgres;
```

Настройки подключения находятся в `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/blog_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

### H2 (Tests)

Для тестов автоматически используется встроенная H2 база данных в памяти.
Настройки находятся в `src/test/resources/application-test.properties`.

## Запуск тестов

### Запуск всех тестов

```bash
./gradlew test
```
## Запуск приложения

### Через Gradle

```bash
./gradlew bootRun
```

### Через JAR-файл

```bash
java -jar build/libs/my-blog-back-app-four-0.0.1-SNAPSHOT.jar
```


Приложение запустится на порту **8080** (по умолчанию).

## Автор

Mirakyan

