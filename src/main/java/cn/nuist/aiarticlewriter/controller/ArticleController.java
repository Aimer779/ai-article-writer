package cn.nuist.aiarticlewriter.controller;

import cn.nuist.aiarticlewriter.agent.support.SseEmitterManager;
import cn.nuist.aiarticlewriter.common.BaseResponse;
import cn.nuist.aiarticlewriter.common.DeleteRequest;
import cn.nuist.aiarticlewriter.common.ResultUtils;
import cn.nuist.aiarticlewriter.exception.ErrorCode;
import cn.nuist.aiarticlewriter.exception.ThrowUtils;
import cn.nuist.aiarticlewriter.model.dto.article.ArticleCreateRequest;
import cn.nuist.aiarticlewriter.model.dto.article.ArticleQueryRequest;
import cn.nuist.aiarticlewriter.model.dto.article.ArticleUpdateRequest;
import cn.nuist.aiarticlewriter.model.entity.Article;
import cn.nuist.aiarticlewriter.model.entity.User;
import cn.nuist.aiarticlewriter.model.vo.ArticleVO;
import cn.nuist.aiarticlewriter.service.ArticleAsyncService;
import cn.nuist.aiarticlewriter.service.ArticleService;
import cn.nuist.aiarticlewriter.service.UserService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
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
        articleAsyncService.executeArticleGeneration(taskId, loginUser.getId(), articleCreateRequest.getTopic(), null);
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
        articleService.getArticleVOByTaskId(taskId, loginUser);

        SseEmitter emitter = sseEmitterManager.createEmitter(taskId);
        log.info("SSE connection established, taskId={}", taskId);
        return emitter;
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
}
