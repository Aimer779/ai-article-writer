# AI Article Writer - Project Overview

A full-stack web application for AI-powered article writing, built with Spring Boot backend and Vue 3 frontend.

## Tech Stack

### Backend

| Category | Technology | Version |
|----------|------------|---------|
| Framework | Spring Boot | 3.5.x |
| Language | Java | 21 |
| ORM | MyBatis-Flex | 1.11.x |
| Database | MySQL | 8.x |
| Cache | Redis | - |
| Session | Spring Session + Redis | - |
| API Doc | Knife4j (OpenAPI 3) | 4.4.x |
| Utility | Hutool | 5.8.x |
| Connection Pool | HikariCP | 4.0.x |
| AOP | Spring AOP | - |

### Frontend

See [frontend/AGENTS.md](./frontend/AGENTS.md)

## Project Structure

```
ai-article-writer/
├── src/main/java/cn/nuist/aiarticlewriter/
│   ├── annotation/        # Custom annotations
│   ├── aop/               # Aspect interceptors
│   ├── common/            # Common response and request helpers
│   ├── config/            # Spring configuration
│   ├── constant/          # Constants
│   ├── controller/        # REST controllers
│   ├── exception/         # Exception handling
│   ├── mapper/            # MyBatis-Flex mappers
│   ├── model/             # Entity, DTO, VO, and enum models
│   └── service/           # Business services
├── src/main/resources/    # Application resources
├── frontend/              # Vue 3 frontend project
├── sql/                   # Database schema scripts
├── pom.xml                # Maven dependencies
└── AGENTS.md              # This file
```

## Package Description

| Package | Description |
|---------|-------------|
| `annotation` | Custom annotations, such as permission checks |
| `aop` | Aspect interceptors for cross-cutting concerns |
| `common` | Common utilities including response wrapper, pagination, and helper classes |
| `config` | Spring configuration classes |
| `constant` | Shared constants |
| `controller` | REST API endpoints, including health checks and user APIs |
| `exception` | Exception handling with error codes and custom exceptions |
| `mapper` | MyBatis-Flex data access mappers |
| `model` | Entities, request DTOs, response VOs, and enums |
| `service` | Business service interfaces and implementations |

## Code Style

- Comments should be written in English.
- Java fields and methods use camelCase.
- API responses should use `BaseResponse` and `ResultUtils`.
- Business errors should use `BusinessException` with `ErrorCode`.
- Controller methods should stay thin and delegate business logic to services.
- Do not expose entity objects directly when sensitive fields may be included; return VO objects instead.

## Key Points

When adding a new business entity, follow this core path:

1. Add database schema in `sql/` if a new table is needed.
2. Add entity class under `model/entity`.
3. Add request DTOs under `model/dto/{module}`.
4. Add response VOs under `model/vo` when entity data needs to be desensitized or reshaped.
5. Add enums under `model/enums` when fixed business states or roles are needed.
6. Add mapper under `mapper`.
7. Add service interface under `service` and implementation under `service/impl`.
8. Add controller under `controller`.
9. Use `BusinessException` and `ErrorCode` for validation and business failures.
10. Use `@AuthCheck` for endpoints that require role-based access control.

## Development

### Backend

```bash
# Run Spring Boot application
mvn spring-boot:run

# Build project
mvn package
```

### Frontend

```bash
# Enter frontend directory
cd frontend

# See frontend development guide
cat AGENTS.md

# Install dependencies
pnpm install

# Start dev server
pnpm dev
```
