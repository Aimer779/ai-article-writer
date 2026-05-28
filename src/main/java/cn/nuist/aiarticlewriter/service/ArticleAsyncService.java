package cn.nuist.aiarticlewriter.service;

import cn.nuist.aiarticlewriter.agent.ArticleAgentOrchestrator;
import cn.nuist.aiarticlewriter.agent.support.SseEmitterManager;
import cn.nuist.aiarticlewriter.model.entity.Article;
import cn.nuist.aiarticlewriter.model.enums.ArticleStepEnum;
import cn.nuist.aiarticlewriter.model.enums.ArticleStatusEnum;
import cn.nuist.aiarticlewriter.model.enums.SseMessageTypeEnum;
import cn.nuist.aiarticlewriter.model.state.article.ArticleState;
import cn.nuist.aiarticlewriter.model.state.article.ImageResult;
import cn.nuist.aiarticlewriter.model.state.article.TitleResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Async service for executing article generation tasks.
 */
@Service
@Slf4j
public class ArticleAsyncService {

    @Resource
    private ArticleAgentOrchestrator articleAgentOrchestrator;

    @Resource
    private SseEmitterManager sseEmitterManager;

    @Resource
    private ArticleService articleService;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * Execute article generation asynchronously.
     *
     * @param taskId article generation task id
     * @param topic article topic
     */
    @Async("articleExecutor")
    public void executeArticleGeneration(String taskId, String topic) {
        generateTitleOptionsAndPause(taskId, null, topic, null);
    }

    /**
     * Generate title options asynchronously and pause for user selection.
     *
     * @param taskId article generation task id
     * @param userId user id
     * @param topic article topic
     * @param userRequirement optional user requirement
     */
    @Async("articleExecutor")
    public void generateTitleOptionsAndPause(String taskId, Long userId, String topic, String userRequirement) {
        log.info("Async title generation started, taskId={}, topic={}", taskId, topic);
        ArticleState state = null;

        try {
            articleService.createArticleTaskIfAbsent(taskId, userId, topic);
            articleService.updateArticleStatus(taskId, ArticleStatusEnum.PROCESSING, null);

            state = articleAgentOrchestrator.createInitialState(taskId, userId, topic);
            articleAgentOrchestrator.generateTitleOptions(state, userRequirement);
            articleService.saveTitleOptionsAndWait(taskId, state, userRequirement);
            sendSseMessage(taskId, SseMessageTypeEnum.AGENT1_COMPLETE, Map.of(
                    "taskId", taskId,
                    "titleOptions", state.getTitleOptions()
            ));
            sendSseMessage(taskId, SseMessageTypeEnum.WAITING_USER_INPUT, Map.of(
                    "taskId", taskId,
                    "step", "TITLE_SELECTION",
                    "message", "Waiting for title selection"
            ));

            log.info("Async title generation paused for user input, taskId={}", taskId);
        } catch (Exception e) {
            handleGenerationFailure(taskId, state, e);
        }
    }

    /**
     * Regenerate title options for a task that has already been prepared by the service layer.
     *
     * @param taskId article generation task id
     */
    @Async("articleExecutor")
    public void regenerateTitleOptions(String taskId) {
        Article article = articleService.getByTaskId(taskId);
        if (article == null) {
            sendSseMessage(taskId, SseMessageTypeEnum.ERROR, Map.of(
                    "taskId", taskId,
                    "message", "Article task does not exist"
            ));
            return;
        }
        generateTitleOptionsAndPause(taskId, article.getUserId(), article.getTopic(), article.getUserRequirement());
    }

    /**
     * Continue article generation after the user selects a title.
     *
     * @param taskId article generation task id
     */
    @Async("articleExecutor")
    public void continueAfterTitleSelected(String taskId) {
        log.info("Async article generation resumed, taskId={}", taskId);
        ArticleState state = null;

        try {
            Article article = articleService.getByTaskId(taskId);
            if (article == null) {
                throw new IllegalStateException("Article task does not exist");
            }
            TitleResult selectedTitle = TitleResult.builder()
                    .mainTitle(article.getMainTitle())
                    .subTitle(article.getSubTitle())
                    .build();
            state = articleAgentOrchestrator.createInitialState(taskId, article.getUserId(), article.getTopic());
            articleAgentOrchestrator.selectTitle(state, selectedTitle);
            sendSseMessage(taskId, SseMessageTypeEnum.AGENT1_COMPLETE, Map.of(
                    "taskId", taskId,
                    "selectedTitle", selectedTitle
            ));

            generateOutlineAndPause(taskId, state, article.getUserRequirement());
            log.info("Async article generation paused for outline review, taskId={}", taskId);
        } catch (Exception e) {
            handleGenerationFailure(taskId, state, e);
        }
    }

    /**
     * Continue article generation after the user confirms the outline.
     *
     * @param taskId article generation task id
     */
    @Async("articleExecutor")
    public void continueAfterOutlineConfirmed(String taskId) {
        log.info("Async article generation resumed after outline review, taskId={}", taskId);
        ArticleState state = null;

        try {
            Article article = articleService.getByTaskId(taskId);
            if (article == null) {
                throw new IllegalStateException("Article task does not exist");
            }
            TitleResult selectedTitle = TitleResult.builder()
                    .mainTitle(article.getMainTitle())
                    .subTitle(article.getSubTitle())
                    .build();
            state = articleAgentOrchestrator.createInitialState(taskId, article.getUserId(), article.getTopic());
            articleAgentOrchestrator.selectTitle(state, selectedTitle);
            state.setOutlineMarkdown(parseOutlineMarkdown(article.getOutline()));

            executePostOutlineGeneration(taskId, state, article.getUserRequirement());
            log.info("Async article generation completed, taskId={}", taskId);
        } catch (Exception e) {
            handleGenerationFailure(taskId, state, e);
        }
    }

    private void generateOutlineAndPause(String taskId, ArticleState state, String userRequirement) {
        articleAgentOrchestrator.generateOutline(state, userRequirement,
                message -> handleAgentMessage(taskId, message, state));
        articleService.saveOutlineAndWait(taskId, state);
        sendSseMessage(taskId, SseMessageTypeEnum.AGENT2_COMPLETE, Map.of(
                "taskId", taskId,
                "outline", state.getOutlineMarkdown()
        ));
        sendSseMessage(taskId, SseMessageTypeEnum.WAITING_USER_INPUT, Map.of(
                "taskId", taskId,
                "step", ArticleStepEnum.OUTLINE_REVIEW.getValue(),
                "message", "Waiting for outline review"
        ));
    }

    private void executePostOutlineGeneration(String taskId, ArticleState state, String userRequirement) {
        articleAgentOrchestrator.generateContent(state, userRequirement,
                message -> handleAgentMessage(taskId, message, state));
        sendSseMessage(taskId, SseMessageTypeEnum.AGENT3_COMPLETE, Map.of(
                "taskId", taskId,
                "content", state.getContent()
        ));

        articleAgentOrchestrator.analyzeImageRequirements(state);
        sendSseMessage(taskId, SseMessageTypeEnum.AGENT4_COMPLETE, Map.of(
                "taskId", taskId,
                "contentWithPlaceholders", state.getContentWithPlaceholders(),
                "imageRequirements", state.getImageRequirements()
        ));

        articleAgentOrchestrator.generateImages(state, message -> handleAgentMessage(taskId, message, state));
        sendSseMessage(taskId, SseMessageTypeEnum.AGENT5_COMPLETE, Map.of(
                "taskId", taskId,
                "images", state.getImages()
        ));

        articleAgentOrchestrator.assembleFullContent(state);
        sendSseMessage(taskId, SseMessageTypeEnum.MERGE_COMPLETE, Map.of(
                "taskId", taskId,
                "fullContent", state.getFullContent()
        ));

        articleService.saveArticleContent(taskId, state);
        articleService.updateArticleStatus(taskId, ArticleStatusEnum.COMPLETED, null);
        sendSseMessage(taskId, SseMessageTypeEnum.ALL_COMPLETE, Map.of("taskId", taskId));
        sseEmitterManager.complete(taskId);
    }

    private String parseOutlineMarkdown(String outlineJson) {
        if (outlineJson == null || outlineJson.isBlank()) {
            throw new IllegalStateException("Article outline does not exist");
        }
        try {
            return objectMapper.readValue(outlineJson, String.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Parse article outline failed", e);
        }
    }

    private void handleGenerationFailure(String taskId, ArticleState state, Exception e) {
        log.error("Async article generation failed, taskId={}", taskId, e);
        if (state != null) {
            state.setStatus(ArticleStatusEnum.FAILED);
            state.setErrorMessage(e.getMessage());
        }
        articleService.updateArticleStatus(taskId, ArticleStatusEnum.FAILED, e.getMessage());
        sendSseMessage(taskId, SseMessageTypeEnum.ERROR, Map.of(
                "taskId", taskId,
                "message", e.getMessage() == null ? "Article generation failed" : e.getMessage()
        ));
        sseEmitterManager.complete(taskId);
    }

    private void handleAgentMessage(String taskId, String message, ArticleState state) {
        Map<String, Object> data = buildMessageData(message, state);
        if (data != null) {
            sendJsonMessage(taskId, data);
        }
    }

    private Map<String, Object> buildMessageData(String message, ArticleState state) {
        if (message == null) {
            return null;
        }

        String outlineStreamingPrefix = SseMessageTypeEnum.AGENT2_STREAMING.getStreamingPrefix();
        String contentStreamingPrefix = SseMessageTypeEnum.AGENT3_STREAMING.getStreamingPrefix();
        String imageCompletePrefix = SseMessageTypeEnum.IMAGE_COMPLETE.getStreamingPrefix();

        if (message.startsWith(outlineStreamingPrefix)) {
            return buildStreamingData(SseMessageTypeEnum.AGENT2_STREAMING,
                    message.substring(outlineStreamingPrefix.length()));
        }
        if (message.startsWith(contentStreamingPrefix)) {
            return buildStreamingData(SseMessageTypeEnum.AGENT3_STREAMING,
                    message.substring(contentStreamingPrefix.length()));
        }
        if (message.startsWith(imageCompletePrefix)) {
            return buildImageCompleteData(message.substring(imageCompletePrefix.length()));
        }
        return buildCompleteMessageData(message, state);
    }

    private Map<String, Object> buildStreamingData(SseMessageTypeEnum type, String content) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", type.getValue());
        data.put("content", content);
        return data;
    }

    private Map<String, Object> buildImageCompleteData(String imageJson) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("type", SseMessageTypeEnum.IMAGE_COMPLETE.getValue());
            data.put("image", objectMapper.readValue(imageJson, ImageResult.class));
            return data;
        } catch (JsonProcessingException e) {
            log.error("Failed to parse image complete message, content={}", imageJson, e);
            return null;
        }
    }

    private Map<String, Object> buildCompleteMessageData(String message, ArticleState state) {
        if (state == null) {
            return null;
        }

        Map<String, Object> data = new HashMap<>();
        if (SseMessageTypeEnum.AGENT1_COMPLETE.getValue().equals(message)) {
            data.put("type", SseMessageTypeEnum.AGENT1_COMPLETE.getValue());
            data.put("taskId", state.getTaskId());
            data.put("titleOptions", state.getTitleOptions());
            data.put("selectedTitle", state.getSelectedTitle());
        } else if (SseMessageTypeEnum.AGENT2_COMPLETE.getValue().equals(message)) {
            data.put("type", SseMessageTypeEnum.AGENT2_COMPLETE.getValue());
            data.put("taskId", state.getTaskId());
            data.put("outline", state.getOutlineMarkdown());
        } else if (SseMessageTypeEnum.AGENT3_COMPLETE.getValue().equals(message)) {
            data.put("type", SseMessageTypeEnum.AGENT3_COMPLETE.getValue());
            data.put("taskId", state.getTaskId());
            data.put("content", state.getContent());
        } else if (SseMessageTypeEnum.AGENT4_COMPLETE.getValue().equals(message)) {
            data.put("type", SseMessageTypeEnum.AGENT4_COMPLETE.getValue());
            data.put("taskId", state.getTaskId());
            data.put("contentWithPlaceholders", state.getContentWithPlaceholders());
            data.put("imageRequirements", state.getImageRequirements());
        } else if (SseMessageTypeEnum.AGENT5_COMPLETE.getValue().equals(message)) {
            data.put("type", SseMessageTypeEnum.AGENT5_COMPLETE.getValue());
            data.put("taskId", state.getTaskId());
            data.put("images", state.getImages());
        } else if (SseMessageTypeEnum.MERGE_COMPLETE.getValue().equals(message)) {
            data.put("type", SseMessageTypeEnum.MERGE_COMPLETE.getValue());
            data.put("taskId", state.getTaskId());
            data.put("fullContent", state.getFullContent());
        } else if (SseMessageTypeEnum.ALL_COMPLETE.getValue().equals(message)) {
            data.put("type", SseMessageTypeEnum.ALL_COMPLETE.getValue());
            data.put("taskId", state.getTaskId());
        } else {
            return null;
        }
        return data;
    }

    private void sendSseMessage(String taskId, SseMessageTypeEnum messageType, Map<String, Object> additionalData) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", messageType.getValue());
        data.putAll(additionalData);
        sendJsonMessage(taskId, data);
    }

    private void sendJsonMessage(String taskId, Map<String, Object> data) {
        try {
            sseEmitterManager.send(taskId, objectMapper.writeValueAsString(data));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize SSE message, taskId={}, data={}", taskId, data, e);
            sseEmitterManager.send(taskId, "{\"type\":\"ERROR\",\"message\":\"Failed to serialize SSE message\"}");
        }
    }
}
