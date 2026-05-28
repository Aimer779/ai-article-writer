# SSE Protocol Reference

Use this when changing progress push, frontend update contracts, or `SseMessageTypeEnum`.

## Transport

- `SseEmitterManager` stores SSE connections by `taskId`.
- Async generation sends progress through the emitter bound to the task.
- Controller endpoints should validate article access before creating an emitter.

## Message Format

Messages sent to the frontend should be JSON objects with a `type` field:

```json
{"type":"AGENT2_STREAMING","content":"..."}
```

Internal agent streaming can still use prefixed strings such as `AGENT2_STREAMING:chunk`; adapt them to JSON before sending to the frontend.

## Message Types

Use `SseMessageTypeEnum` as the source of truth:

- `AGENT1_COMPLETE`
- `WAITING_USER_INPUT`
- `AGENT2_STREAMING`
- `AGENT2_COMPLETE`
- `AGENT3_STREAMING`
- `AGENT3_COMPLETE`
- `AGENT4_COMPLETE`
- `IMAGE_COMPLETE`
- `AGENT5_COMPLETE`
- `MERGE_COMPLETE`
- `ALL_COMPLETE`
- `ERROR`

## Title Selection Pause

After title generation, the backend sends:

```json
{"type":"AGENT1_COMPLETE","taskId":"...","titleOptions":[{"mainTitle":"...","subTitle":"..."}]}
```

Then it sends:

```json
{"type":"WAITING_USER_INPUT","taskId":"...","step":"TITLE_SELECTION","message":"Waiting for title selection"}
```

The frontend should show title options and keep the task open. Selecting a title calls `POST /article/{taskId}/title/select`; adding title direction calls `POST /article/{taskId}/title/regenerate`.

## Outline Review Pause

After outline generation, the backend sends:

```json
{"type":"AGENT2_COMPLETE","taskId":"...","outline":"## Section\n..."}
```

Then it sends:

```json
{"type":"WAITING_USER_INPUT","taskId":"...","step":"OUTLINE_REVIEW","message":"Waiting for outline review"}
```

The frontend should show the outline in an editable Markdown textarea. Confirming the edited outline calls `POST /article/{taskId}/outline/confirm`, then content streaming resumes.
