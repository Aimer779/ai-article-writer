package cn.nuist.aiarticlewriter.agent;

import cn.nuist.aiarticlewriter.agent.agents.ContentAgent;
import cn.nuist.aiarticlewriter.agent.agents.ImageRequirementAgent;
import cn.nuist.aiarticlewriter.agent.agents.OutlineAgent;
import cn.nuist.aiarticlewriter.agent.agents.TitleAgent;
import cn.nuist.aiarticlewriter.model.enums.ArticleStatusEnum;
import cn.nuist.aiarticlewriter.model.enums.ArticleStepEnum;
import cn.nuist.aiarticlewriter.model.state.article.ArticleState;
import cn.nuist.aiarticlewriter.model.state.article.ImageRequirement;
import cn.nuist.aiarticlewriter.model.state.article.OutlineResult;
import cn.nuist.aiarticlewriter.model.state.article.TitleResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Serial orchestrator for article generation agents.
 */
@Component
@RequiredArgsConstructor
public class ArticleAgentOrchestrator {

    private final TitleAgent titleAgent;

    private final OutlineAgent outlineAgent;

    private final ContentAgent contentAgent;

    private final ImageRequirementAgent imageRequirementAgent;

    private final ArticleAgentContextFactory contextFactory;

    private final ArticleAgentResultValidator resultValidator;

    /**
     * Create a new state and run the full article generation workflow.
     *
     * @param taskId generation task ID
     * @param userId user ID
     * @param topic article topic
     * @param userRequirement optional user requirement
     * @return completed article state
     */
    public ArticleState generate(String taskId, Long userId, String topic, String userRequirement) {
        ArticleState state = ArticleState.builder()
                .taskId(taskId)
                .userId(userId)
                .topic(topic == null ? null : topic.trim())
                .currentStep(ArticleStepEnum.INIT)
                .status(ArticleStatusEnum.PENDING)
                .build();
        return generate(state, userRequirement);
    }

    /**
     * Run the full article generation workflow with an existing state.
     *
     * @param state article state
     * @param userRequirement optional user requirement
     * @return completed article state
     */
    public ArticleState generate(ArticleState state, String userRequirement) {
        if (state == null) {
            throw new ArticleAgentException("Article state cannot be null");
        }
        try {
            resultValidator.validateTopic(state.getTopic());
            state.setStatus(ArticleStatusEnum.PROCESSING);
            String normalizedRequirement = contextFactory.normalizeUserRequirement(userRequirement);

            runTitleStep(state, normalizedRequirement);
            runOutlineStep(state, normalizedRequirement);
            runContentStep(state, normalizedRequirement);
            runImageRequirementStep(state);

            state.setCurrentStep(ArticleStepEnum.COMPLETED);
            state.setStatus(ArticleStatusEnum.COMPLETED);
            state.setErrorMessage(null);
            return state;
        } catch (ArticleAgentException e) {
            markFailed(state, e);
            throw e;
        } catch (RuntimeException e) {
            ArticleAgentException agentException = new ArticleAgentException("Article agent workflow failed", e);
            markFailed(state, agentException);
            throw agentException;
        }
    }

    private void runTitleStep(ArticleState state, String userRequirement) {
        state.setCurrentStep(ArticleStepEnum.TITLE);
        TitleResult title = titleAgent.generate(state.getTopic(), userRequirement);
        resultValidator.validateTitle(title);
        state.setTitle(title);
    }

    private void runOutlineStep(ArticleState state, String userRequirement) {
        state.setCurrentStep(ArticleStepEnum.OUTLINE);
        TitleResult title = state.getTitle();
        OutlineResult outline = outlineAgent.generate(state.getTopic(), title.getMainTitle(), title.getSubTitle(),
                userRequirement);
        resultValidator.validateOutline(outline);
        state.setOutline(outline);
    }

    private void runContentStep(ArticleState state, String userRequirement) {
        state.setCurrentStep(ArticleStepEnum.CONTENT);
        TitleResult title = state.getTitle();
        String outlineJson = contextFactory.toOutlineJson(state.getOutline());
        String content = contentAgent.generate(state.getTopic(), title.getMainTitle(), title.getSubTitle(), outlineJson,
                userRequirement);
        resultValidator.validateContent(content, state.getOutline());
        state.setContent(content);
    }

    private void runImageRequirementStep(ArticleState state) {
        state.setCurrentStep(ArticleStepEnum.IMAGE_REQUIREMENT);
        TitleResult title = state.getTitle();
        String outlineJson = contextFactory.toOutlineJson(state.getOutline());
        List<ImageRequirement> imageRequirements = imageRequirementAgent.analyze(title.getMainTitle(),
                title.getSubTitle(), outlineJson, state.getContent());
        resultValidator.validateImageRequirements(imageRequirements, title, state.getOutline(), state.getContent());
        state.setImageRequirements(imageRequirements);
    }

    private void markFailed(ArticleState state, ArticleAgentException e) {
        state.setStatus(ArticleStatusEnum.FAILED);
        state.setErrorMessage(e.getMessage());
    }
}
