package cn.nuist.aiarticlewriter.agent.agents;

import cn.nuist.aiarticlewriter.agent.support.ArticleLlmClient;
import cn.nuist.aiarticlewriter.constant.PromptConstant;
import cn.nuist.aiarticlewriter.model.state.article.ImageRequirement;
import cn.nuist.aiarticlewriter.model.state.article.TitleResult;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Agent responsible for analyzing article image requirements.
 */
@Component
@RequiredArgsConstructor
public class ImageRequirementAgent {

    private final ArticleLlmClient articleLlmClient;

    /**
     * Analyze image requirements from generated article content.
     *
     * @param selectedTitle selected title
     * @param outlineMarkdown outline Markdown
     * @param contentMarkdown article Markdown
     * @return image requirements
     */
    public List<ImageRequirement> analyzeImageRequirements(TitleResult selectedTitle, String outlineMarkdown,
            String contentMarkdown) {
        String prompt = articleLlmClient.renderPrompt(PromptConstant.IMAGE_REQUIREMENT_ANALYSIS_PROMPT, Map.of(
                "mainTitle", selectedTitle.getMainTitle(),
                "subTitle", selectedTitle.getSubTitle(),
                "outlineMarkdown", outlineMarkdown,
                "contentMarkdown", contentMarkdown));
        String content = articleLlmClient.callLlm(prompt);
        return articleLlmClient.parseJsonListResponse(content, new TypeReference<>() {
        }, "Image requirements");
    }
}
