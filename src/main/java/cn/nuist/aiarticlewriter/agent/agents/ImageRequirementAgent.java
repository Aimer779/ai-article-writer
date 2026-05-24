package cn.nuist.aiarticlewriter.agent.agents;

import cn.nuist.aiarticlewriter.constant.PromptConstant;
import cn.nuist.aiarticlewriter.model.state.article.ImageRequirement;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.util.List;

/**
 * Agent responsible for analyzing article image requirements.
 */
public interface ImageRequirementAgent {

    @UserMessage(PromptConstant.IMAGE_REQUIREMENT_ANALYSIS_PROMPT)
    List<ImageRequirement> analyze(@V("mainTitle") String mainTitle, @V("subTitle") String subTitle,
            @V("outlineJson") String outlineJson, @V("contentMarkdown") String contentMarkdown);
}
