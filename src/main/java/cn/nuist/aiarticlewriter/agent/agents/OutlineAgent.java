package cn.nuist.aiarticlewriter.agent.agents;

import cn.nuist.aiarticlewriter.constant.PromptConstant;
import cn.nuist.aiarticlewriter.model.state.article.OutlineResult;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Agent responsible for article outline generation.
 */
public interface OutlineAgent {

    @UserMessage(PromptConstant.OUTLINE_GENERATION_PROMPT)
    OutlineResult generate(@V("topic") String topic, @V("mainTitle") String mainTitle,
            @V("subTitle") String subTitle, @V("userRequirement") String userRequirement);
}
