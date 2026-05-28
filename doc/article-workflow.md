# Article Workflow Reference

Use this when changing article generation agents, orchestration, or persistence.

## Workflow

The current workflow is:

1. `TitleAgent`: generate 3 to 5 title options from the topic.
2. Pause at `TITLE_SELECTION` with `WAITING_USER_INPUT` until the user selects a title.
3. If the user adds title direction, rerun `TitleAgent` and replace the candidate title options.
4. `OutlineAgent`: stream a Markdown outline from the selected title.
5. Pause at `OUTLINE_REVIEW` with `WAITING_USER_INPUT` until the user edits and confirms the outline.
6. `ContentAgent`: stream Markdown article content from the confirmed outline.
7. `ImageRequirementAgent`: analyze content and produce image requirements.
8. `ImageGenerationAgent`: acquire images from search or generation methods.
9. `ArticleContentAssembler`: merge images into Markdown content. This is not an agent.

## State

- Runtime state belongs in `model/state/article`.
- Typed outputs should stay serializable and match prompt contracts.
- `article` is the task/result table; it stores compact workflow state such as `titleOptions`, `userRequirement`, `outline`, and `currentStep`.
- `article.outline` stores the generated or confirmed Markdown outline as a JSON-serialized string.
- Do not store raw LLM call details in `article`.
- `ArticleService` owns article CRUD, task creation, status updates, and generated content persistence.

## Async Tasks

- Create the article task before starting async generation.
- Use `@Async("articleExecutor")` for article generation background work.
- Status should flow through `PENDING`, `PROCESSING`, `WAITING_USER_INPUT`, `COMPLETED`, or `FAILED`.
- Use `taskId` to connect persisted task state, async execution, and SSE progress.
- The initial create action only runs title generation and pauses.
- Title selection starts outline generation and pauses again for outline review.
- Outline confirmation starts content, image, and final assembly generation.

## Image Requirements

- Use `type = cover` for `position = 1`.
- Use `type = section` for section images starting at `position = 2`.
- `sectionTitle` must exactly match the Markdown section heading.
