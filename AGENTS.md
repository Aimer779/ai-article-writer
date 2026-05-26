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
| LLM Integration | LangChain4j | 1.15.x beta |

### Frontend

See [frontend/AGENTS.md](./frontend/AGENTS.md)

## Project Structure

```
ai-article-writer/
├── src/main/java/cn/nuist/aiarticlewriter/
│   ├── annotation/        # Custom annotations
│   ├── agent/             # Article generation agents and orchestration support
│   ├── aop/               # Aspect interceptors
│   ├── common/            # Common response and request helpers
│   ├── config/            # Spring configuration
│   ├── constant/          # Constants
│   ├── controller/        # REST controllers
│   ├── exception/         # Exception handling
│   ├── mapper/            # MyBatis-Flex mappers
│   ├── model/             # Entity, DTO, VO, enum, and workflow state models
│   └── service/           # Business services
├── src/main/resources/    # Application resources
├── doc/                   # On-demand development reference docs
├── frontend/              # Vue 3 frontend project
├── sql/                   # Database schema scripts
├── pom.xml                # Maven dependencies
└── AGENTS.md              # This file
```

## Package Description

| Package | Description |
|---------|-------------|
| `annotation` | Custom annotations, such as permission checks |
| `agent` | Article generation agents, orchestration, LLM helpers, content assembly, and SSE support |
| `aop` | Aspect interceptors for cross-cutting concerns |
| `common` | Common utilities including response wrapper, pagination, and helper classes |
| `config` | Spring configuration classes |
| `constant` | Shared constants, including centralized AI prompt templates |
| `controller` | REST API endpoints, including health checks and user APIs |
| `exception` | Exception handling with error codes and custom exceptions |
| `mapper` | MyBatis-Flex data access mappers |
| `model` | Entities, request DTOs, response VOs, enums, and workflow state models |
| `service` | Business service interfaces and implementations |

## Code Style

- Comments should be written in English.
- Log messages should be written in English.
- Java fields and methods use camelCase.
- API responses should use `BaseResponse` and `ResultUtils`.
- Business errors should use `BusinessException` with `ErrorCode`.
- Controller methods should stay thin and delegate business logic to services.
- Do not expose entity objects directly when sensitive fields may be included; return VO objects instead.

## Key Points

Read these reference docs only when the task is related:

- Article generation workflow: `doc/article-workflow.md`
- SSE message protocol: `doc/sse-protocol.md`
- Image search and storage: `doc/image-service.md`
- Runtime configuration and tests: `doc/runtime-config-and-tests.md`

When adding backend business modules, follow the existing entity -> DTO -> VO -> mapper -> service -> controller structure. Use `BaseResponse` / `ResultUtils`, `BusinessException` / `ErrorCode`, and return VO objects instead of exposing entities directly. For MyBatis-Flex entities, use `@Table(..., camelToUnderline = false)` when table columns use camelCase.

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
