package cn.nuist.aiarticlewriter.agent.agents;

import cn.nuist.aiarticlewriter.model.state.article.TitleResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Slf4j
class TitleAgentTest {

    @Autowired
    private TitleAgent titleAgent;

    @Value("${langchain4j.open-ai.chat-model.api-key:}")
    private String apiKey;

    @Test
    void shouldGenerateTitleOptionsWithRealLlm() {
        Assumptions.assumeTrue(apiKey != null && !apiKey.isBlank(), "AI model API key is not configured");

        List<TitleResult> titleOptions = titleAgent.generateTitleOptions(
                "How AI agents improve article writing workflows",
                "Use a practical tone for software engineers");

        for (int i = 0; i < titleOptions.size(); i++) {
            TitleResult title = titleOptions.get(i);
            log.info("Generated title option {}: mainTitle={}, subTitle={}", i + 1, title.getMainTitle(),
                    title.getSubTitle());
        }

        assertThat(titleOptions).hasSizeBetween(3, 5);
        assertThat(titleOptions)
                .allSatisfy(title -> {
                    assertThat(title.getMainTitle()).isNotBlank();
                    assertThat(title.getSubTitle()).isNotBlank();
                });
    }
}
