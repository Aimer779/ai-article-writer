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
├── src/
│   └── main/
│       ├── java/cn/nuist/aiarticlewriter/
│       │   ├── AiArticleWriterApplication.java    # Application entry point
│       │   ├── common/                            # Common utilities
│       │   │   ├── BaseResponse.java              # Unified response wrapper
│       │   │   ├── ResultUtils.java               # Response construction helper
│       │   │   ├── PageRequest.java               # Pagination request base
│       │   │   └── DeleteRequest.java             # Delete request DTO
│       │   ├── config/                            # Configuration
│       │   │   └── CorsConfig.java                # CORS configuration
│       │   ├── controller/                        # API controllers
│       │   │   └── HealthController.java          # Health check endpoint
│       │   └── exception/                         # Exception handling
│       │       ├── ErrorCode.java                 # Error code enum
│       │       ├── BusinessException.java         # Custom business exception
│       │       ├── GlobalExceptionHandler.java    # Global exception handler
│       │       └── ThrowUtils.java                # Exception throwing utility
│       └── resources/
│           └── application.yml                    # Application config
├── frontend/                                      # Vue 3 frontend project
├── pom.xml                                        # Maven dependencies
└── AGENTS.md                                      # This file
```

## Package Description

| Package | Description |
|---------|-------------|
| `common` | Common utilities including response wrapper, pagination, and helper classes |
| `config` | Spring configuration classes (CORS, etc.) |
| `controller` | REST API endpoints |
| `exception` | Exception handling with error codes, custom exceptions, and global handler |

## Code Style

<!-- TODO: Define code style guidelines -->

## Key Points

<!-- TODO: Document key development notes and best practices -->

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
