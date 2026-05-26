# Article Workflow Reference

Use this when changing article generation agents, orchestration, or persistence.

## Workflow

The current workflow is:

1. `TitleAgent`: generate 3 to 5 title options from the topic.
2. `OutlineAgent`: stream a Markdown outline from the selected title.
3. `ContentAgent`: stream Markdown article content from the outline.
4. `ImageRequirementAgent`: analyze content and produce image requirements.
5. `ImageGenerationAgent`: acquire images from search or generation methods.
6. `ArticleContentAssembler`: merge images into Markdown content. This is not an agent.

## State

- Runtime state belongs in `model/state/article`.
- Typed outputs should stay serializable and match prompt contracts.
- `article` is the task/result table; do not store raw LLM call details there.
- `ArticleService` owns article CRUD, task creation, status updates, and generated content persistence.

## Async Tasks

- Create the article task before starting async generation.
- Use `@Async("articleExecutor")` for article generation background work.
- Status should flow through `PENDING`, `PROCESSING`, `COMPLETED`, or `FAILED`.
- Use `taskId` to connect persisted task state, async execution, and SSE progress.

## Image Requirements

- Use `type = cover` for `position = 1`.
- Use `type = section` for section images starting at `position = 2`.
- `sectionTitle` must exactly match the Markdown section heading.
