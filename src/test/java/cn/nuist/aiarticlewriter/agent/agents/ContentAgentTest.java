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
class ContentAgentTest {

    @Autowired
    private ContentAgent contentAgent;

    @Value("${langchain4j.open-ai.streaming-chat-model.api-key:}")
    private String streamingApiKey;

    @Test
    @Timeout(180)
    void shouldGenerateContentWithRealStreamingLlm() {
        Assumptions.assumeTrue(streamingApiKey != null && !streamingApiKey.isBlank(),
                "Streaming AI model API key is not configured");

        TitleResult selectedTitle = TitleResult.builder()
                .mainTitle("Building Reliable AI Article Workflows")
                .subTitle("A practical guide to staged generation with dedicated agents")
                .build();
        String outlineMarkdown = """
                ## Why Workflow Boundaries Matter
                - Separate title selection, outline generation, content generation, and image planning.
                - Keep long-running generation stages streamable.

                ## Designing Agent Responsibilities
                - Give each agent one stable responsibility.
                - Use deterministic service code for final Markdown assembly.
                """;
        List<String> streamChunks = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger chunkIndex = new AtomicInteger();

        String contentMarkdown = contentAgent.generateContent(
                "How AI agents improve article writing workflows",
                selectedTitle,
                outlineMarkdown,
                "Write a concise article for software engineers. Keep each section short.",
                chunk -> {
                    streamChunks.add(chunk);
                    log.info("Generated content stream chunk {}: {}", chunkIndex.incrementAndGet(), chunk);
                });

        String streamingPrefix = SseMessageTypeEnum.CONTENT.getStreamingPrefix();
        String streamedContent = streamChunks.stream()
                .map(chunk -> chunk.substring(streamingPrefix.length()))
                .reduce("", String::concat);

        log.info("Generated content chunk count: {}", streamChunks.size());
        log.info("Generated content markdown:\n{}", contentMarkdown);

        assertThat(contentMarkdown).isNotBlank();
        assertThat(contentMarkdown).contains("##");
        assertThat(streamChunks).isNotEmpty();
        assertThat(streamChunks).allSatisfy(chunk -> assertThat(chunk).startsWith(streamingPrefix));
        assertThat(streamedContent).isEqualTo(contentMarkdown);
    }
}
