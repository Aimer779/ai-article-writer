package cn.nuist.aiarticlewriter.agent.agents;

import cn.nuist.aiarticlewriter.constant.PromptConstant;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * Agent responsible for writing Markdown article content.
 */
public interface ContentAgent {

    @UserMessage(PromptConstant.CONTENT_CREATION_PROMPT)
    String generate(@V("topic") String topic, @V("mainTitle") String mainTitle, @V("subTitle") String subTitle,
            @V("outlineJson") String outlineJson, @V("userRequirement") String userRequirement);
}
