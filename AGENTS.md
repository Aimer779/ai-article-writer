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
| Image Search | Pexels, Bing image scraping | - |
| Image Generation | DashScope Qwen-Image, Mermaid CLI, LLM-generated SVG | - |
| Object Storage | Tencent Cloud COS | 5.6.x |
| HTML Parsing | Jsoup | 1.15.x |
| Payments | Stripe Checkout + Webhook | stripe-java 29.x |

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
| `agent` | Article generation agents, orchestration, LLM helpers, content assembly, execution logging, and SSE support |
| `aop` | Aspect interceptors for cross-cutting concerns, including auth checks and agent execution logging |
| `common` | Common utilities including response wrapper, pagination, and helper classes |
| `config` | Spring configuration classes and runtime properties for LLM, image providers, COS storage, and Stripe payments |
| `constant` | Shared constants, including centralized AI prompt templates |
| `controller` | REST API endpoints, including health checks, user APIs, article APIs, and payment APIs |
| `exception` | Exception handling with error codes and custom exceptions |
| `mapper` | MyBatis-Flex data access mappers |
| `model` | Entities, request DTOs, response VOs, enums, and workflow state models |
| `service` | Business service interfaces and implementations, including article persistence, image acquisition, storage adapters, and payment processing |

## Code Style

- Comments should be written in English.
- Log messages should be written in English.
- Java fields and methods use camelCase.
- Controller methods should stay thin: validate request shape, get the current user when needed, call services, and wrap responses.
- API responses must use `BaseResponse` and `ResultUtils`; do not return raw data directly from controllers.
- Parameter validation should use `ThrowUtils.throwIf(...)` with `ErrorCode`.
- Use `ThrowUtils.throwIf(...)` for conditional guards; create `BusinessException` directly only when a non-guard exception object is required.
- Do not expose entity objects directly when sensitive fields may be included; return VO objects instead.
- Use DTO objects for request payloads and VO objects for response payloads.
- Avoid broad `BeanUtils.copyProperties` for security-sensitive updates or fields with different semantics; map important fields explicitly.
- Keep mappers minimal and place business rules in services, not controllers or mapper interfaces.
- Add `@Transactional` on service methods when a business operation performs multiple related database writes.
- For MyBatis-Flex entities, use `@Table(..., camelToUnderline = false)` when table columns use camelCase, and use `@Column(isLogicDelete = true)` for logic-delete fields.

## Key Points

Read these reference docs only when the task is related:

- Article generation workflow: `doc/article-workflow.md`
- Title human-in-the-loop workflow: `doc/title-hitl-workflow.md`
- SSE message protocol: `doc/sse-protocol.md`
- Image acquisition and storage: `doc/image-service.md`
- Runtime configuration and tests: `doc/runtime-config-and-tests.md`

Title human-in-the-loop follows: create task -> generate 3 to 5 title options -> persist `titleOptions` -> set `currentStep=TITLE_SELECTION` and `status=WAITING_USER_INPUT` -> user selects a title or adds requirements to regenerate titles -> selected title resumes outline generation. Only allow title selection/regeneration while the task is in `WAITING_USER_INPUT` and `TITLE_SELECTION`.

Outline human-in-the-loop follows: selected title -> generate Markdown outline -> persist `outline` -> set `currentStep=OUTLINE_REVIEW` and `status=WAITING_USER_INPUT` -> user edits and confirms the outline -> confirmed outline resumes content/image/final assembly generation. Reuse the existing `article.outline` field as a JSON-serialized Markdown string; no extra migration is required.

Image generation follows: select provider -> acquire `ImageAsset` -> upload to storage -> fallback to Picsum on failure. New image providers should implement `ImageService`, return `ImageAsset`, and register their method in `ImageMethodEnum`. Do not return temporary third-party image URLs as final article image URLs when COS is enabled.

Stripe payment follows: authenticated user -> create `payment_record` with `PENDING` status -> create Stripe Checkout Session -> handle Stripe webhook -> mark payment `SUCCEEDED` only when Stripe reports `payment_status=paid` -> activate VIP membership by setting `vipTime` and upgrading normal users to `vip`. Keep webhook handling idempotent and verify signatures with `STRIPE_WEBHOOK_SECRET`.

Agent execution logging follows: annotate Spring-managed public agent stage methods with `@AgentExecution` -> extract `taskId` from `ArticleState` -> store compact JSON input/output summaries in `agent_log` -> save logs asynchronously. Keep log messages in English and avoid storing full generated article content or sensitive prompt text unless explicitly required.

When adding backend business modules, follow the existing entity -> DTO -> VO -> mapper -> service -> controller structure. Keep controllers thin, put business rules in services, and follow the Code Style rules above for responses, errors, validation, VO mapping, and MyBatis-Flex entity annotations.

## Development

### Backend

```bash
# Run Spring Boot application
mvn spring-boot:run

# Build project
mvn package

# Compile backend
mvn compile

# Run focused tests
mvn test -Dtest=ClassName

# Apply VIP/payment migration in PowerShell
Get-Content sql/add_payment_vip.sql | mysql -u root -p

# Apply agent log migration in PowerShell
Get-Content sql/add_agent_log.sql | mysql -u root -p

# Apply title human-in-the-loop migration in PowerShell
Get-Content sql/add_title_hitl.sql | mysql -u root -p
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
