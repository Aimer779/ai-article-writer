# Runtime Configuration And Tests

Use this when changing local model configuration or real integration tests.

## LangChain4j

Chat model configuration belongs in `application-local.yml` or environment variables:

- `langchain4j.open-ai.chat-model.api-key`
- `langchain4j.open-ai.chat-model.base-url`
- `langchain4j.open-ai.chat-model.model-name`

Streaming model configuration is required for outline and content streaming:

- `langchain4j.open-ai.streaming-chat-model.api-key`
- `langchain4j.open-ai.streaming-chat-model.base-url`
- `langchain4j.open-ai.streaming-chat-model.model-name`

## Tests

- Agent integration tests may call a real LLM.
- Pexels integration tests require `image-search.pexels.api-key`.
- Do not commit `application-local.yml` secrets or API keys.
