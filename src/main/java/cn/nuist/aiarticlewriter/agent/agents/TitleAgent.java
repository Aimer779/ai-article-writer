package cn.nuist.aiarticlewriter.agent.agents;

import cn.nuist.aiarticlewriter.constant.PromptConstant;
import cn.nuist.aiarticlewriter.model.state.article.TitleResult;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Agent responsible for article title generation.
 */
public interface TitleAgent {

    @UserMessage(PromptConstant.TITLE_GENERATION_PROMPT)
    TitleResult generate(@V("topic") String topic, @V("userRequirement") String userRequirement);
}
