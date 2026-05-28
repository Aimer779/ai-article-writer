# Title Human-in-the-Loop Workflow

Use this when changing the title selection pause, regeneration behavior, or frontend title decision UI.

## Flow

1. `POST /article/create` creates the article task and starts async title generation.
2. The title agent generates 3 to 5 candidate titles.
3. The backend persists `titleOptions`, sets `currentStep = TITLE_SELECTION`, and sets `status = WAITING_USER_INPUT`.
4. The frontend receives `AGENT1_COMPLETE` and `WAITING_USER_INPUT`, then displays title options.
5. `POST /article/{taskId}/title/select` saves the selected title and resumes outline, content, image, and assembly stages.
6. `POST /article/{taskId}/title/regenerate` appends the additional requirement, reruns title generation, and replaces `titleOptions`.

## Persistence

- `titleOptions`: JSON array of `TitleResult` objects.
- `userRequirement`: accumulated user direction used by later agents.
- `currentStep`: workflow step value from `ArticleStepEnum`.
- `mainTitle` and `subTitle`: final selected title only.

## Guards

- Only the task owner or an admin may select or regenerate titles.
- Title selection and title regeneration require `status = WAITING_USER_INPUT` and `currentStep = TITLE_SELECTION`.
- Regeneration requires a non-blank additional requirement.
- Selecting a title requires a valid zero-based title index.

## Database Migration

Apply `sql/add_title_hitl.sql` before using this workflow on an existing database.
