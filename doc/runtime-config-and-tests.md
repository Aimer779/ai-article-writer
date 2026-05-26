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
- AI image generation tests require `ai-image.api-key`.
- AI image full-chain tests require DashScope and COS configuration.
- COS integration tests require `image-storage.cos.*` credentials.
- Real external integration tests are disabled by default or guarded by explicit flags.
- Do not commit `application-local.yml` secrets or API keys.

Run stable AI image tests:

```bash
mvn test -Dtest=AiImageGenerationServiceImplTest,AiImageGenerationServiceImplIntegrationTest
```

Run real DashScope image generation:

```bash
mvn test -Dtest=AiImageGenerationServiceImplIntegrationTest -Dai-image.integration-test.enabled=true
```

Run AI image generation plus COS upload:

```bash
mvn test -Dtest=ImageGenerationAgentAiImageIntegrationTest -Dai-image.full-chain-test.enabled=true
```

Run COS upload integration test:

```bash
mvn test -Dtest=CosImageStorageServiceImplTest -Dcos.test.enabled=true
```
