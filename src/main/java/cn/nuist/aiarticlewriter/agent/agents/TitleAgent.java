package cn.nuist.aiarticlewriter.agent.agents;

import cn.nuist.aiarticlewriter.agent.support.ArticleLlmClient;
import cn.nuist.aiarticlewriter.constant.PromptConstant;
import cn.nuist.aiarticlewriter.model.state.article.TitleResult;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Agent responsible for generating candidate article titles.
 */
@Component
@RequiredArgsConstructor
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
        return articleLlmClient.parseJsonListResponse(content, new TypeReference<>() {
        }, "Title options");
    }
}
