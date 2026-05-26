package cn.nuist.aiarticlewriter.agent.agents;

import cn.nuist.aiarticlewriter.agent.support.ArticleLlmClient;
import cn.nuist.aiarticlewriter.constant.PromptConstant;
import cn.nuist.aiarticlewriter.model.enums.SseMessageTypeEnum;
import cn.nuist.aiarticlewriter.model.state.article.TitleResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Agent responsible for writing Markdown article content.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContentAgent {

    private final ArticleLlmClient articleLlmClient;

    /**
     * Generate article Markdown with streaming output.
     *
     * @param topic article topic
     * @param selectedTitle selected title
     * @param outlineMarkdown outline Markdown
     * @param userRequirement optional user requirement
     * @param streamHandler stream handler
     * @return complete article Markdown
     */
    public String generateContent(String topic, TitleResult selectedTitle, String outlineMarkdown, String userRequirement,
            Consumer<String> streamHandler) {
        String prompt = articleLlmClient.renderPrompt(PromptConstant.CONTENT_CREATION_PROMPT, Map.of(
                "topic", topic,
                "mainTitle", selectedTitle.getMainTitle(),
                "subTitle", selectedTitle.getSubTitle(),
                "outlineMarkdown", outlineMarkdown,
                "userRequirement", userRequirement));
        String content = articleLlmClient.callLlmWithStreaming(prompt, streamHandler,
                SseMessageTypeEnum.AGENT3_STREAMING);
        log.info("ContentAgent generated content, mainTitle={}, length={}", selectedTitle.getMainTitle(),
                content.length());
        return content;
    }
}
