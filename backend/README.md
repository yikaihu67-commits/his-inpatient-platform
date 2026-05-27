# Backend

HIS inpatient backend service built with Java 21, Maven, and Spring Boot 3.

## Requirements

- JDK 21
- Maven 3.9+
- PostgreSQL running locally with database `his_inpatient`

## Configuration

Default database settings are in `src/main/resources/application.yml`:

- URL: `jdbc:postgresql://localhost:5432/his_inpatient`
- Username: `postgres`
- Password: `123456`
- Server port: `8080`

## Start

```powershell
cd backend
mvn spring-boot:run
```

Health check:

```powershell
curl http://localhost:8080/api/health
```
