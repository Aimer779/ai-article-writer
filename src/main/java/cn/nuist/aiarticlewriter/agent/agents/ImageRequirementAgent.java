package cn.nuist.aiarticlewriter.agent.agents;

import cn.nuist.aiarticlewriter.agent.support.ArticleLlmClient;
import cn.nuist.aiarticlewriter.constant.PromptConstant;
import cn.nuist.aiarticlewriter.model.state.article.Agent4Result;
import cn.nuist.aiarticlewriter.model.state.article.ImageRequirement;
import cn.nuist.aiarticlewriter.model.state.article.TitleResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Agent responsible for analyzing article image requirements.
 */
@Component
@RequiredArgsConstructor
@Slf4j
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
        return analyzeImagePlan(selectedTitle, outlineMarkdown, contentMarkdown).getImageRequirements();
    }

    /**
     * Analyze image requirements and insert image placeholders into article content.
     *
     * @param selectedTitle selected title
     * @param outlineMarkdown outline Markdown
     * @param contentMarkdown article Markdown
     * @return agent 4 image analysis result
     */
    public Agent4Result analyzeImagePlan(TitleResult selectedTitle, String outlineMarkdown, String contentMarkdown) {
        String prompt = articleLlmClient.renderPrompt(PromptConstant.IMAGE_REQUIREMENT_ANALYSIS_PROMPT, Map.of(
                "mainTitle", selectedTitle.getMainTitle(),
                "subTitle", selectedTitle.getSubTitle(),
                "outlineMarkdown", outlineMarkdown,
                "contentMarkdown", contentMarkdown));
        String content = articleLlmClient.callLlm(prompt);
        Agent4Result result = articleLlmClient.parseJsonResponse(content, Agent4Result.class, "Agent 4 image plan");
        int count = result.getImageRequirements() == null ? 0 : result.getImageRequirements().size();
        log.info("ImageRequirementAgent generated image plan, mainTitle={}, count={}",
                selectedTitle.getMainTitle(), count);
        return result;
    }
}
