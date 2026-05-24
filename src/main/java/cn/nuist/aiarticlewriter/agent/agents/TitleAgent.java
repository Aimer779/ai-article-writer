package cn.nuist.aiarticlewriter.agent.agents;

import cn.nuist.aiarticlewriter.agent.support.ArticleLlmClient;
import cn.nuist.aiarticlewriter.constant.PromptConstant;
import cn.nuist.aiarticlewriter.model.state.article.TitleResult;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Agent responsible for generating candidate article titles.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TitleAgent {

    private final ArticleLlmClient articleLlmClient;

    /**
     * Generate 3 to 5 title options for user selection.
     *
     * @param topic article topic
     * @param userRequirement optional user requirement
     * @return candidate title results
     */
    public List<TitleResult> generateTitleOptions(String topic, String userRequirement) {
        String prompt = articleLlmClient.renderPrompt(PromptConstant.TITLE_GENERATION_PROMPT, Map.of(
                "topic", topic,
                "userRequirement", userRequirement));
        String content = articleLlmClient.callLlm(prompt);
        List<TitleResult> titleOptions = articleLlmClient.parseJsonListResponse(content, new TypeReference<>() {
        }, "Title options");
        log.info("TitleAgent generated title options, topic={}, count={}", topic, titleOptions.size());
        return titleOptions;
    }
}
