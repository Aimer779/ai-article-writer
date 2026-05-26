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
