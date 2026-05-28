package cn.nuist.aiarticlewriter.controller;

import cn.hutool.core.util.StrUtil;
import cn.nuist.aiarticlewriter.agent.support.SseEmitterManager;
import cn.nuist.aiarticlewriter.common.BaseResponse;
import cn.nuist.aiarticlewriter.common.DeleteRequest;
import cn.nuist.aiarticlewriter.common.ResultUtils;
import cn.nuist.aiarticlewriter.exception.ErrorCode;
import cn.nuist.aiarticlewriter.exception.ThrowUtils;
import cn.nuist.aiarticlewriter.model.dto.article.ArticleCreateRequest;
import cn.nuist.aiarticlewriter.model.dto.article.ArticleOutlineConfirmRequest;
import cn.nuist.aiarticlewriter.model.dto.article.ArticleQueryRequest;
import cn.nuist.aiarticlewriter.model.dto.article.ArticleTitleRegenerateRequest;
import cn.nuist.aiarticlewriter.model.dto.article.ArticleTitleSelectRequest;
import cn.nuist.aiarticlewriter.model.dto.article.ArticleUpdateRequest;
import cn.nuist.aiarticlewriter.model.entity.Article;
import cn.nuist.aiarticlewriter.model.entity.User;
import cn.nuist.aiarticlewriter.model.enums.ArticleStatusEnum;
import cn.nuist.aiarticlewriter.model.enums.ArticleStepEnum;
import cn.nuist.aiarticlewriter.model.enums.SseMessageTypeEnum;
import cn.nuist.aiarticlewriter.model.state.article.TitleResult;
import cn.nuist.aiarticlewriter.model.vo.ArticleVO;
import cn.nuist.aiarticlewriter.service.ArticleAsyncService;
import cn.nuist.aiarticlewriter.service.ArticleService;
import cn.nuist.aiarticlewriter.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Article controller.
 */
@RestController
@RequestMapping("/article")
@Tag(name = "Article Controller")
@Slf4j
public class ArticleController {

    @Resource
    private ArticleService articleService;

    @Resource
    private ArticleAsyncService articleAsyncService;

    @Resource
    private SseEmitterManager sseEmitterManager;

    @Resource
    private UserService userService;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * Create an article generation task.
     *
     * @param articleCreateRequest article creation request
     * @param request HTTP request
     * @return article generation task id
     */
    @PostMapping("/create")
    @Operation(summary = "Create article task")
    public BaseResponse<String> createArticleTask(@RequestBody ArticleCreateRequest articleCreateRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(articleCreateRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        String taskId = articleService.createArticleTask(articleCreateRequest.getTopic(), loginUser);
        articleAsyncService.generateTitleOptionsAndPause(taskId, loginUser.getId(), articleCreateRequest.getTopic(),
                null);
        return ResultUtils.success(taskId);
    }

    /**
     * Get article generation progress by SSE.
     *
     * @param taskId article generation task id
     * @param request HTTP request
     * @return SSE emitter
     */
    @GetMapping("/progress/{taskId}")
    @Operation(summary = "Get article generation progress")
    public SseEmitter getProgress(@PathVariable String taskId, HttpServletRequest request) {
        ThrowUtils.throwIf(taskId == null || taskId.trim().isEmpty(), ErrorCode.PARAMS_ERROR,
                "Task id cannot be blank");
        User loginUser = userService.getLoginUser(request);
        ArticleVO articleVO = articleService.getArticleVOByTaskId(taskId, loginUser);

        SseEmitter emitter = sseEmitterManager.createEmitter(taskId);
        sendWaitingSnapshotIfNeeded(taskId, articleVO);
        log.info("SSE connection established, taskId={}", taskId);
        return emitter;
    }

    /**
     * Select a generated title option and continue article generation.
     *
     * @param taskId article generation task id
     * @param requestBody title selection request
     * @param request HTTP request
     * @return selection result
     */
    @PostMapping("/{taskId}/title/select")
    @Operation(summary = "Select article title")
    public BaseResponse<Boolean> selectTitle(@PathVariable String taskId,
            @RequestBody ArticleTitleSelectRequest requestBody, HttpServletRequest request) {
        ThrowUtils.throwIf(requestBody == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        TitleResult selectedTitle = articleService.selectTitleOption(taskId, requestBody.getTitleIndex(), loginUser);
        articleAsyncService.continueAfterTitleSelected(taskId);
        log.info("Article title selected by user, taskId={}, userId={}, mainTitle={}", taskId, loginUser.getId(),
                selectedTitle.getMainTitle());
        return ResultUtils.success(true);
    }

    /**
     * Confirm reviewed outline and continue article generation.
     *
     * @param taskId article generation task id
     * @param requestBody outline confirm request
     * @param request HTTP request
     * @return confirm result
     */
    @PostMapping("/{taskId}/outline/confirm")
    @Operation(summary = "Confirm article outline")
    public BaseResponse<Boolean> confirmOutline(@PathVariable String taskId,
            @RequestBody ArticleOutlineConfirmRequest requestBody, HttpServletRequest request) {
        ThrowUtils.throwIf(requestBody == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        articleService.confirmOutline(taskId, requestBody.getOutlineMarkdown(), loginUser);
        articleAsyncService.continueAfterOutlineConfirmed(taskId);
        log.info("Article outline confirmed by user, taskId={}, userId={}", taskId, loginUser.getId());
        return ResultUtils.success(true);
    }

    /**
     * Regenerate title options with additional user requirement.
     *
     * @param taskId article generation task id
     * @param requestBody title regeneration request
     * @param request HTTP request
     * @return regeneration result
     */
    @PostMapping("/{taskId}/title/regenerate")
    @Operation(summary = "Regenerate article title options")
    public BaseResponse<Boolean> regenerateTitles(@PathVariable String taskId,
            @RequestBody ArticleTitleRegenerateRequest requestBody, HttpServletRequest request) {
        ThrowUtils.throwIf(requestBody == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        articleService.prepareTitleRegeneration(taskId, requestBody.getAdditionalRequirement(), loginUser);
        articleAsyncService.regenerateTitleOptions(taskId);
        log.info("Article title regeneration requested, taskId={}, userId={}", taskId, loginUser.getId());
        return ResultUtils.success(true);
    }

    /**
     * Get article by id.
     *
     * @param id article id
     * @param request HTTP request
     * @return article view object
     */
    @GetMapping("/get")
    @Operation(summary = "Get article by id")
    public BaseResponse<ArticleVO> getArticleById(@RequestParam Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ArticleVO articleVO = articleService.getArticleVOById(id, loginUser);
        return ResultUtils.success(articleVO);
    }

    /**
     * Get article by task id.
     *
     * @param taskId article generation task id
     * @param request HTTP request
     * @return article view object
     */
    @GetMapping("/get/task")
    @Operation(summary = "Get article by task id")
    public BaseResponse<ArticleVO> getArticleByTaskId(@RequestParam String taskId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ArticleVO articleVO = articleService.getArticleVOByTaskId(taskId, loginUser);
        return ResultUtils.success(articleVO);
    }

    /**
     * Get article by task id.
     *
     * @param taskId article generation task id
     * @param request HTTP request
     * @return article view object
     */
    @GetMapping("/{taskId}")
    @Operation(summary = "Get article by task id")
    public BaseResponse<ArticleVO> getArticleByTaskIdPath(@PathVariable String taskId, HttpServletRequest request) {
        ThrowUtils.throwIf(taskId == null || taskId.trim().isEmpty(), ErrorCode.PARAMS_ERROR,
                "Task id cannot be blank");
        User loginUser = userService.getLoginUser(request);
        ArticleVO articleVO = articleService.getArticleVOByTaskId(taskId, loginUser);
        return ResultUtils.success(articleVO);
    }

    /**
     * Page articles.
     *
     * @param articleQueryRequest article query request
     * @param request HTTP request
     * @return article page
     */
    @PostMapping("/list/page")
    @Operation(summary = "Page articles")
    public BaseResponse<Page<ArticleVO>> listArticleByPage(@RequestBody ArticleQueryRequest articleQueryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(articleQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Page<ArticleVO> articlePage = articleService.listArticleByPage(articleQueryRequest, loginUser);
        return ResultUtils.success(articlePage);
    }

    /**
     * Page articles.
     *
     * @param articleQueryRequest article query request
     * @param request HTTP request
     * @return article page
     */
    @PostMapping("/list")
    @Operation(summary = "Page articles")
    public BaseResponse<Page<ArticleVO>> listArticle(@RequestBody ArticleQueryRequest articleQueryRequest,
            HttpServletRequest request) {
        return listArticleByPage(articleQueryRequest, request);
    }

    /**
     * Update article.
     *
     * @param articleUpdateRequest article update request
     * @param request HTTP request
     * @return update result
     */
    @PostMapping("/update")
    @Operation(summary = "Update article")
    public BaseResponse<Boolean> updateArticle(@RequestBody ArticleUpdateRequest articleUpdateRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(articleUpdateRequest == null || articleUpdateRequest.getId() == null
                || articleUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Article article = new Article();
        BeanUtils.copyProperties(articleUpdateRequest, article);
        boolean result = articleService.updateArticle(article, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * Delete article.
     *
     * @param deleteRequest delete request
     * @param request HTTP request
     * @return delete result
     */
    @PostMapping("/delete")
    @Operation(summary = "Delete article")
    public BaseResponse<Boolean> deleteArticle(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = articleService.deleteArticle(deleteRequest.getId(), loginUser);
        return ResultUtils.success(result);
    }

    private void sendWaitingSnapshotIfNeeded(String taskId, ArticleVO articleVO) {
        sendTitleSelectionSnapshotIfNeeded(taskId, articleVO);
        sendOutlineReviewSnapshotIfNeeded(taskId, articleVO);
    }

    private void sendTitleSelectionSnapshotIfNeeded(String taskId, ArticleVO articleVO) {
        if (articleVO == null
                || !ArticleStatusEnum.WAITING_USER_INPUT.getValue().equals(articleVO.getStatus())
                || !ArticleStepEnum.TITLE_SELECTION.getValue().equals(articleVO.getCurrentStep())
                || StrUtil.isBlank(articleVO.getTitleOptions())) {
            return;
        }

        Map<String, Object> titleData = new HashMap<>();
        titleData.put("type", SseMessageTypeEnum.AGENT1_COMPLETE.getValue());
        titleData.put("taskId", taskId);
        try {
            titleData.put("titleOptions", objectMapper.readValue(articleVO.getTitleOptions(), Object.class));
            sseEmitterManager.send(taskId, objectMapper.writeValueAsString(titleData));

            Map<String, Object> waitingData = new HashMap<>();
            waitingData.put("type", SseMessageTypeEnum.WAITING_USER_INPUT.getValue());
            waitingData.put("taskId", taskId);
            waitingData.put("step", ArticleStepEnum.TITLE_SELECTION.getValue());
            waitingData.put("message", "Waiting for title selection");
            sseEmitterManager.send(taskId, objectMapper.writeValueAsString(waitingData));
        } catch (JsonProcessingException e) {
            log.error("Failed to send title selection snapshot, taskId={}", taskId, e);
        }
    }

    private void sendOutlineReviewSnapshotIfNeeded(String taskId, ArticleVO articleVO) {
        if (articleVO == null
                || !ArticleStatusEnum.WAITING_USER_INPUT.getValue().equals(articleVO.getStatus())
                || !ArticleStepEnum.OUTLINE_REVIEW.getValue().equals(articleVO.getCurrentStep())
                || StrUtil.isBlank(articleVO.getOutline())) {
            return;
        }

        Map<String, Object> outlineData = new HashMap<>();
        outlineData.put("type", SseMessageTypeEnum.AGENT2_COMPLETE.getValue());
        outlineData.put("taskId", taskId);
        try {
            outlineData.put("outline", objectMapper.readValue(articleVO.getOutline(), String.class));
            sseEmitterManager.send(taskId, objectMapper.writeValueAsString(outlineData));

            Map<String, Object> waitingData = new HashMap<>();
            waitingData.put("type", SseMessageTypeEnum.WAITING_USER_INPUT.getValue());
            waitingData.put("taskId", taskId);
            waitingData.put("step", ArticleStepEnum.OUTLINE_REVIEW.getValue());
            waitingData.put("message", "Waiting for outline review");
            sseEmitterManager.send(taskId, objectMapper.writeValueAsString(waitingData));
        } catch (JsonProcessingException e) {
            log.error("Failed to send outline review snapshot, taskId={}", taskId, e);
        }
    }
}
