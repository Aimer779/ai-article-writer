package cn.nuist.aiarticlewriter.config;

import cn.nuist.aiarticlewriter.agent.agents.ContentAgent;
import cn.nuist.aiarticlewriter.agent.agents.ImageRequirementAgent;
import cn.nuist.aiarticlewriter.agent.agents.OutlineAgent;
import cn.nuist.aiarticlewriter.agent.agents.TitleAgent;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LangChain4j AI Service configuration for article agents.
 */
@Configuration
public class ArticleAgentConfig {

    @Bean
    public TitleAgent titleAgent(ChatModel chatModel) {
        return AiServices.builder(TitleAgent.class)
                .chatModel(chatModel)
                .build();
    }

    @Bean
    public OutlineAgent outlineAgent(ChatModel chatModel) {
        return AiServices.builder(OutlineAgent.class)
                .chatModel(chatModel)
                .build();
    }

    @Bean
    public ContentAgent contentAgent(ChatModel chatModel) {
        return AiServices.builder(ContentAgent.class)
                .chatModel(chatModel)
                .build();
    }

    @Bean
    public ImageRequirementAgent imageRequirementAgent(ChatModel chatModel) {
        return AiServices.builder(ImageRequirementAgent.class)
                .chatModel(chatModel)
                .build();
    }
}
