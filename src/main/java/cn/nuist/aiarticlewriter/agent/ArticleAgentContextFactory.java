package cn.nuist.aiarticlewriter.agent;

import cn.nuist.aiarticlewriter.model.state.article.OutlineResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Builds serialized context values passed between article agents.
 */
@Component
@RequiredArgsConstructor
public class ArticleAgentContextFactory {

    private final ObjectMapper objectMapper;

    /**
     * Serialize outline state for prompt input.
     *
     * @param outline outline result
     * @return JSON string
     */
    public String toOutlineJson(OutlineResult outline) {
        try {
            return objectMapper.writeValueAsString(outline);
        } catch (JsonProcessingException e) {
            throw new ArticleAgentException("Failed to serialize outline result", e);
        }
    }

    /**
     * Normalize optional user requirements before sending them to agents.
     *
     * @param userRequirement optional user requirement
     * @return normalized requirement text
     */
    public String normalizeUserRequirement(String userRequirement) {
        if (userRequirement == null || userRequirement.isBlank()) {
            return "No additional requirements.";
        }
        return userRequirement.trim();
    }
}
