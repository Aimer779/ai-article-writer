package cn.nuist.aiarticlewriter;

import dev.langchain4j.model.chat.ChatModel;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AiModelConnectionTest {

    @Autowired
    private ChatModel chatModel;

    @Value("${langchain4j.open-ai.chat-model.api-key:}")
    private String apiKey;

    @Test
    void shouldConnectToConfiguredAiModel() {
        Assumptions.assumeTrue(apiKey != null && !apiKey.isBlank(), "AI model API key is not configured");

        String response = chatModel.chat("Reply with exactly one word: OK");

        assertThat(response).isNotBlank();
        assertThat(response).containsIgnoringCase("OK");
    }
}
