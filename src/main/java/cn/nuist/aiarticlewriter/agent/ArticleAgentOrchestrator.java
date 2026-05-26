package cn.nuist.aiarticlewriter.agent;

import cn.nuist.aiarticlewriter.agent.agents.ContentAgent;
import cn.nuist.aiarticlewriter.agent.agents.ImageGenerationAgent;
import cn.nuist.aiarticlewriter.agent.agents.ImageRequirementAgent;
import cn.nuist.aiarticlewriter.agent.agents.OutlineAgent;
import cn.nuist.aiarticlewriter.agent.agents.TitleAgent;
import cn.nuist.aiarticlewriter.agent.support.ArticleContentAssembler;
import cn.nuist.aiarticlewriter.model.enums.ArticleStatusEnum;
import cn.nuist.aiarticlewriter.model.enums.ArticleStepEnum;
import cn.nuist.aiarticlewriter.model.state.article.Agent4Result;
import cn.nuist.aiarticlewriter.model.state.article.ArticleState;
import cn.nuist.aiarticlewriter.model.state.article.ImageRequirement;
import cn.nuist.aiarticlewriter.model.state.article.ImageResult;
import cn.nuist.aiarticlewriter.model.state.article.TitleResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

/**
 * Stage-based orchestrator for article generation agents.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ArticleAgentOrchestrator {

    private final TitleAgent titleAgent;

    private final OutlineAgent outlineAgent;

    private final ContentAgent contentAgent;

    private final ImageRequirementAgent imageRequirementAgent;

    private final ImageGenerationAgent imageGenerationAgent;

    private final ArticleContentAssembler articleContentAssembler;

    private final ArticleAgentContextFactory contextFactory;

    private final ArticleAgentResultValidator resultValidator;

    /**
     * Create initial article state.
     *
     * @param taskId generation task ID
     * @param userId user ID
     * @param topic article topic
     * @return initialized article state
     */
    public ArticleState createInitialState(String taskId, Long userId, String topic) {
        ArticleState state = ArticleState.builder()
                .taskId(taskId)
                .userId(userId)
                .topic(topic == null ? null : topic.trim())
                .currentStep(ArticleStepEnum.INIT)
                .status(ArticleStatusEnum.PENDING)
                .build();
        resultValidator.validateTopic(state.getTopic());
        return state;
    }

    /**
     * Generate candidate titles and stop for user selection.
     *
     * @param state article state
     * @param userRequirement optional user requirement
     * @return updated article state
     */
    public ArticleState generateTitleOptions(ArticleState state, String userRequirement) {
        return runStage(state, ArticleStepEnum.TITLE, () -> {
            log.info("Starting title option generation, taskId={}, topic={}", state.getTaskId(), state.getTopic());
            String normalizedRequirement = contextFactory.normalizeUserRequirement(userRequirement);
            List<TitleResult> titleOptions = titleAgent.generateTitleOptions(state.getTopic(), normalizedRequirement);
            resultValidator.validateTitleOptions(titleOptions);
            state.setTitleOptions(titleOptions);
            state.setCurrentStep(ArticleStepEnum.TITLE);
            state.setStatus(ArticleStatusEnum.PENDING);
            log.info("Title option generation completed, taskId={}, count={}", state.getTaskId(), titleOptions.size());
        });
    }

    /**
     * Select one title option before outline generation.
     *
     * @param state article state
     * @param selectedTitle selected title
     * @return updated article state
     */
    public ArticleState selectTitle(ArticleState state, TitleResult selectedTitle) {
        resultValidator.validateTitle(selectedTitle);
        state.setSelectedTitle(selectedTitle);
        state.setTitle(selectedTitle);
        state.setCurrentStep(ArticleStepEnum.OUTLINE);
        state.setStatus(ArticleStatusEnum.PENDING);
        state.setErrorMessage(null);
        log.info("Article title selected, taskId={}, mainTitle={}", state.getTaskId(), selectedTitle.getMainTitle());
        return state;
    }

    /**
     * Generate Markdown outline with streaming output.
     *
     * @param state article state
     * @param userRequirement optional user requirement
     * @param streamHandler streaming handler
     * @return updated article state
     */
    public ArticleState generateOutline(ArticleState state, String userRequirement, Consumer<String> streamHandler) {
        return runStage(state, ArticleStepEnum.OUTLINE, () -> {
            TitleResult selectedTitle = requireSelectedTitle(state);
            log.info("Starting outline generation, taskId={}, mainTitle={}", state.getTaskId(),
                    selectedTitle.getMainTitle());
            String normalizedRequirement = contextFactory.normalizeUserRequirement(userRequirement);
            String outlineMarkdown = outlineAgent.generateOutline(state.getTopic(), selectedTitle, normalizedRequirement,
                    streamHandler);
            resultValidator.validateMarkdown("Outline", outlineMarkdown);
            state.setOutlineMarkdown(outlineMarkdown);
            log.info("Outline generation completed, taskId={}, length={}", state.getTaskId(), outlineMarkdown.length());
        });
    }

    /**
     * Generate Markdown article content with streaming output.
     *
     * @param state article state
     * @param userRequirement optional user requirement
     * @param streamHandler streaming handler
     * @return updated article state
     */
    public ArticleState generateContent(ArticleState state, String userRequirement, Consumer<String> streamHandler) {
        return runStage(state, ArticleStepEnum.CONTENT, () -> {
            TitleResult selectedTitle = requireSelectedTitle(state);
            log.info("Starting content generation, taskId={}, mainTitle={}", state.getTaskId(),
                    selectedTitle.getMainTitle());
            resultValidator.validateMarkdown("Outline", state.getOutlineMarkdown());
            String normalizedRequirement = contextFactory.normalizeUserRequirement(userRequirement);
            String content = contentAgent.generateContent(state.getTopic(), selectedTitle, state.getOutlineMarkdown(),
                    normalizedRequirement, streamHandler);
            resultValidator.validateMarkdown("Article content", content);
            state.setContent(content);
            log.info("Content generation completed, taskId={}, length={}", state.getTaskId(), content.length());
        });
    }

    /**
     * Analyze image requirements from article content.
     *
     * @param state article state
     * @return updated article state
     */
    public ArticleState analyzeImageRequirements(ArticleState state) {
        return runStage(state, ArticleStepEnum.IMAGE_REQUIREMENT, () -> {
            TitleResult selectedTitle = requireSelectedTitle(state);
            log.info("Starting image requirement analysis, taskId={}", state.getTaskId());
            resultValidator.validateMarkdown("Outline", state.getOutlineMarkdown());
            resultValidator.validateMarkdown("Article content", state.getContent());
            Agent4Result agent4Result = imageRequirementAgent.analyzeImagePlan(selectedTitle,
                    state.getOutlineMarkdown(), state.getContent());
            resultValidator.validateAgent4Result(agent4Result, selectedTitle, state.getContent());
            List<ImageRequirement> imageRequirements = agent4Result.getImageRequirements();
            state.setContentWithPlaceholders(agent4Result.getContentWithPlaceholders());
            state.setImageRequirements(imageRequirements);
            log.info("Image requirement analysis completed, taskId={}, count={}", state.getTaskId(),
                    imageRequirements.size());
        });
    }

    /**
     * Generate or search images from image requirements.
     *
     * @param state article state
     * @return updated article state
     */
    public ArticleState generateImages(ArticleState state) {
        return generateImages(state, null);
    }

    /**
     * Generate or search images from image requirements with streaming output.
     *
     * @param state article state
     * @param streamHandler streaming handler
     * @return updated article state
     */
    public ArticleState generateImages(ArticleState state, Consumer<String> streamHandler) {
        return runStage(state, ArticleStepEnum.IMAGE_GENERATION, () -> {
            log.info("Starting image generation, taskId={}", state.getTaskId());
            resultValidator.validateImageRequirementsExist(state.getImageRequirements());
            List<ImageResult> images = imageGenerationAgent.generateImages(state.getImageRequirements(), streamHandler);
            resultValidator.validateImageResults(images);
            state.setImages(images);
            images.stream()
                    .filter(image -> image.getPosition() != null && image.getPosition() == 1)
                    .findFirst()
                    .ifPresent(image -> state.setCoverImage(image.getUrl()));
            log.info("Image generation completed, taskId={}, count={}", state.getTaskId(), images.size());
        });
    }

    /**
     * Assemble complete Markdown content with generated images.
     *
     * @param state article state
     * @return updated article state
     */
    public ArticleState assembleFullContent(ArticleState state) {
        return runStage(state, ArticleStepEnum.ASSEMBLE, () -> {
            log.info("Starting article content assembly, taskId={}", state.getTaskId());
            resultValidator.validateMarkdown("Article content", state.getContent());
            resultValidator.validateImageResults(state.getImages());
            String content = state.getContentWithPlaceholders() == null || state.getContentWithPlaceholders().isBlank()
                    ? state.getContent()
                    : state.getContentWithPlaceholders();
            String fullContent = articleContentAssembler.assemble(content, state.getImages());
            state.setFullContent(fullContent);
            state.setCurrentStep(ArticleStepEnum.COMPLETED);
            state.setStatus(ArticleStatusEnum.COMPLETED);
            log.info("Article content assembly completed, taskId={}, length={}", state.getTaskId(),
                    fullContent.length());
        });
    }

    private ArticleState runStage(ArticleState state, ArticleStepEnum step, Runnable runnable) {
        if (state == null) {
            throw new ArticleAgentException("Article state cannot be null");
        }
        try {
            state.setCurrentStep(step);
            state.setStatus(ArticleStatusEnum.PROCESSING);
            state.setErrorMessage(null);
            runnable.run();
            if (!ArticleStepEnum.COMPLETED.equals(state.getCurrentStep())) {
                state.setCurrentStep(step);
                state.setStatus(ArticleStatusEnum.PENDING);
            }
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

    private TitleResult requireSelectedTitle(ArticleState state) {
        TitleResult selectedTitle = state.getSelectedTitle() == null ? state.getTitle() : state.getSelectedTitle();
        if (selectedTitle == null) {
            throw new ArticleAgentException("Selected title is required");
        }
        resultValidator.validateTitle(selectedTitle);
        return selectedTitle;
    }

    private void markFailed(ArticleState state, ArticleAgentException e) {
        state.setStatus(ArticleStatusEnum.FAILED);
        state.setErrorMessage(e.getMessage());
        log.info("Article agent stage failed, taskId={}, step={}, error={}", state.getTaskId(), state.getCurrentStep(),
                e.getMessage());
    }
}
