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
 * Agent responsible for generating Markdown article outlines.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutlineAgent {

    private final ArticleLlmClient articleLlmClient;

    /**
     * Generate outline Markdown with streaming output.
     *
     * @param topic article topic
     * @param selectedTitle selected title
     * @param userRequirement optional user requirement
     * @param streamHandler stream handler
     * @return complete outline Markdown
     */
    public String generateOutline(String topic, TitleResult selectedTitle, String userRequirement,
            Consumer<String> streamHandler) {
        String prompt = articleLlmClient.renderPrompt(PromptConstant.OUTLINE_GENERATION_PROMPT, Map.of(
                "topic", topic,
                "mainTitle", selectedTitle.getMainTitle(),
                "subTitle", selectedTitle.getSubTitle(),
                "userRequirement", userRequirement));
        String outlineMarkdown = articleLlmClient.callLlmWithStreaming(prompt, streamHandler, SseMessageTypeEnum.OUTLINE);
        log.info("OutlineAgent generated outline, mainTitle={}, length={}", selectedTitle.getMainTitle(),
                outlineMarkdown.length());
        return outlineMarkdown;
    }
}
