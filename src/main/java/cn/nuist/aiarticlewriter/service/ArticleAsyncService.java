package cn.nuist.aiarticlewriter.service;

import cn.nuist.aiarticlewriter.agent.ArticleAgentOrchestrator;
import cn.nuist.aiarticlewriter.agent.support.SseEmitterManager;
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
        executeArticleGeneration(taskId, null, topic, null);
    }

    /**
     * Execute article generation asynchronously.
     *
     * @param taskId article generation task id
     * @param userId user id
     * @param topic article topic
     * @param userRequirement optional user requirement
     */
    @Async("articleExecutor")
    public void executeArticleGeneration(String taskId, Long userId, String topic, String userRequirement) {
        log.info("Async article generation started, taskId={}, topic={}", taskId, topic);
        ArticleState state = null;

        try {
            articleService.createArticleTaskIfAbsent(taskId, userId, topic);
            articleService.updateArticleStatus(taskId, ArticleStatusEnum.PROCESSING, null);

            state = articleAgentOrchestrator.createInitialState(taskId, userId, topic);
            articleAgentOrchestrator.generateTitleOptions(state, userRequirement);
            TitleResult selectedTitle = state.getTitleOptions().getFirst();
            articleAgentOrchestrator.selectTitle(state, selectedTitle);
            sendSseMessage(taskId, SseMessageTypeEnum.AGENT1_COMPLETE, Map.of(
                    "taskId", taskId,
                    "titleOptions", state.getTitleOptions(),
                    "selectedTitle", selectedTitle
            ));

            ArticleState currentState = state;
            articleAgentOrchestrator.generateOutline(state, userRequirement,
                    message -> handleAgentMessage(taskId, message, currentState));
            sendSseMessage(taskId, SseMessageTypeEnum.AGENT2_COMPLETE, Map.of(
                    "taskId", taskId,
                    "outline", state.getOutlineMarkdown()
            ));

            articleAgentOrchestrator.generateContent(state, userRequirement,
                    message -> handleAgentMessage(taskId, message, currentState));
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

            articleAgentOrchestrator.generateImages(state, message -> handleAgentMessage(taskId, message, currentState));
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

            log.info("Async article generation completed, taskId={}", taskId);
        } catch (Exception e) {
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
