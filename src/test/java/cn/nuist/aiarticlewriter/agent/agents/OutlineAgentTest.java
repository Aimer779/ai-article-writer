package cn.nuist.aiarticlewriter.agent.agents;

import cn.nuist.aiarticlewriter.model.enums.SseMessageTypeEnum;
import cn.nuist.aiarticlewriter.model.state.article.TitleResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Slf4j
class OutlineAgentTest {

    @Autowired
    private OutlineAgent outlineAgent;

    @Value("${langchain4j.open-ai.streaming-chat-model.api-key:}")
    private String streamingApiKey;

    @Test
    @Timeout(120)
    void shouldGenerateOutlineWithRealStreamingLlm() {
        Assumptions.assumeTrue(streamingApiKey != null && !streamingApiKey.isBlank(),
                "Streaming AI model API key is not configured");

        TitleResult selectedTitle = TitleResult.builder()
                .mainTitle("Building Reliable AI Article Workflows")
                .subTitle("A practical guide to staged generation with dedicated agents")
                .build();
        List<String> streamChunks = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger chunkIndex = new AtomicInteger();

        String outlineMarkdown = outlineAgent.generateOutline(
                "How AI agents improve article writing workflows",
                selectedTitle,
                "Write for software engineers. Keep the outline practical and implementation-oriented.",
                chunk -> {
                    streamChunks.add(chunk);
                    log.info("Generated outline stream chunk {}: {}", chunkIndex.incrementAndGet(), chunk);
                });

        String streamingPrefix = SseMessageTypeEnum.OUTLINE.getStreamingPrefix();
        String streamedOutline = streamChunks.stream()
                .map(chunk -> chunk.substring(streamingPrefix.length()))
                .reduce("", String::concat);

        log.info("Generated outline chunk count: {}", streamChunks.size());
        log.info("Generated outline markdown:\n{}", outlineMarkdown);

        assertThat(outlineMarkdown).isNotBlank();
        assertThat(outlineMarkdown).contains("#");
        assertThat(streamChunks).isNotEmpty();
        assertThat(streamChunks).allSatisfy(chunk -> assertThat(chunk).startsWith(streamingPrefix));
        assertThat(streamedOutline).isEqualTo(outlineMarkdown);
    }
}
