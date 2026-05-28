package cn.nuist.aiarticlewriter.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.nuist.aiarticlewriter.constant.UserConstant;
import cn.nuist.aiarticlewriter.exception.BusinessException;
import cn.nuist.aiarticlewriter.exception.ErrorCode;
import cn.nuist.aiarticlewriter.exception.ThrowUtils;
import cn.nuist.aiarticlewriter.mapper.ArticleMapper;
import cn.nuist.aiarticlewriter.model.dto.article.ArticleQueryRequest;
import cn.nuist.aiarticlewriter.model.entity.Article;
import cn.nuist.aiarticlewriter.model.entity.User;
import cn.nuist.aiarticlewriter.model.enums.ArticleStepEnum;
import cn.nuist.aiarticlewriter.model.enums.ArticleStatusEnum;
import cn.nuist.aiarticlewriter.model.state.article.ArticleState;
import cn.nuist.aiarticlewriter.model.state.article.ImageResult;
import cn.nuist.aiarticlewriter.model.state.article.TitleResult;
import cn.nuist.aiarticlewriter.model.vo.ArticleVO;
import cn.nuist.aiarticlewriter.service.ArticleService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * Article persistence service implementation.
 */
@Service
@Slf4j
public class ArticleServiceImpl implements ArticleService {

    private static final long DEFAULT_USER_ID = 0L;

    private static final long MAX_PAGE_SIZE = 50;

    private static final Set<String> ARTICLE_SORT_FIELD_SET = Set.of(
            "id",
            "createTime",
            "completedTime",
            "updateTime"
    );

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public String createArticleTask(String topic, User loginUser) {
        validateLoginUser(loginUser);
        ThrowUtils.throwIf(StrUtil.isBlank(topic), ErrorCode.PARAMS_ERROR, "Topic cannot be blank");

        String taskId = IdUtil.simpleUUID();
        LocalDateTime now = LocalDateTime.now();
        Article article = Article.builder()
                .taskId(taskId)
                .userId(loginUser.getId())
                .topic(topic.trim())
                .currentStep(ArticleStepEnum.INIT.getValue())
                .status(ArticleStatusEnum.PENDING.getValue())
                .createTime(now)
                .updateTime(now)
                .isDelete(0)
                .build();

        int result = articleMapper.insert(article);
        ThrowUtils.throwIf(result <= 0 || article.getId() == null, ErrorCode.OPERATION_ERROR,
                "Create article task failed");
        log.info("Article task created, taskId={}, userId={}", taskId, loginUser.getId());
        return taskId;
    }

    @Override
    public ArticleVO getArticleVO(Article article) {
        if (article == null) {
            return null;
        }
        ArticleVO articleVO = new ArticleVO();
        BeanUtils.copyProperties(article, articleVO);
        return articleVO;
    }

    @Override
    public ArticleVO getArticleVOById(long id, User loginUser) {
        validateLoginUser(loginUser);
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR, "Article id is invalid");
        Article article = articleMapper.selectOneById(id);
        ThrowUtils.throwIf(article == null, ErrorCode.NOT_FOUND_ERROR, "Article does not exist");
        validateArticleAccess(article, loginUser);
        return getArticleVO(article);
    }

    @Override
    public ArticleVO getArticleVOByTaskId(String taskId, User loginUser) {
        validateLoginUser(loginUser);
        validateTaskId(taskId);
        Article article = getByTaskId(taskId);
        ThrowUtils.throwIf(article == null, ErrorCode.NOT_FOUND_ERROR, "Article task does not exist");
        validateArticleAccess(article, loginUser);
        return getArticleVO(article);
    }

    @Override
    public Article getByTaskId(String taskId) {
        validateTaskId(taskId);
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("taskId", taskId);
        return articleMapper.selectOneByQuery(queryWrapper);
    }

    @Override
    public Page<ArticleVO> listArticleByPage(ArticleQueryRequest request, User loginUser) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        Article article = new Article();
        BeanUtils.copyProperties(request, article);
        return pageArticleVO(request.getPageNum(), request.getPageSize(), article, request.getSortField(),
                request.getSortOrder(), loginUser);
    }

    @Override
    public Page<ArticleVO> pageArticleVO(long pageNumber, long pageSize, Article article, String sortField,
            String sortOrder, User loginUser) {
        validateLoginUser(loginUser);
        ThrowUtils.throwIf(pageNumber <= 0, ErrorCode.PARAMS_ERROR, "Page number is invalid");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > MAX_PAGE_SIZE, ErrorCode.PARAMS_ERROR,
                "Page size is invalid");

        QueryWrapper queryWrapper = QueryWrapper.create();
        if (article != null) {
            queryWrapper
                    .eq("id", article.getId(), article.getId() != null)
                    .eq("taskId", article.getTaskId(), StrUtil.isNotBlank(article.getTaskId()))
                    .eq("userId", article.getUserId(), isAdmin(loginUser) && article.getUserId() != null)
                    .like("topic", article.getTopic(), StrUtil.isNotBlank(article.getTopic()))
                    .like("mainTitle", article.getMainTitle(), StrUtil.isNotBlank(article.getMainTitle()))
                    .eq("status", article.getStatus(), StrUtil.isNotBlank(article.getStatus()));
        }
        if (!isAdmin(loginUser)) {
            queryWrapper.eq("userId", loginUser.getId());
        }

        String finalSortField = StrUtil.blankToDefault(sortField, "createTime");
        ThrowUtils.throwIf(!ARTICLE_SORT_FIELD_SET.contains(finalSortField), ErrorCode.PARAMS_ERROR,
                "Sort field is invalid");
        boolean isAsc = "ascend".equals(sortOrder);
        queryWrapper.orderBy(finalSortField, isAsc);

        Page<Article> articlePage = articleMapper.paginate(pageNumber, pageSize, queryWrapper);
        return articlePage.map(this::getArticleVO);
    }

    @Override
    public boolean updateArticle(Article article, User loginUser) {
        validateLoginUser(loginUser);
        ThrowUtils.throwIf(article == null || article.getId() == null || article.getId() <= 0,
                ErrorCode.PARAMS_ERROR, "Article id is invalid");

        Article oldArticle = articleMapper.selectOneById(article.getId());
        ThrowUtils.throwIf(oldArticle == null, ErrorCode.NOT_FOUND_ERROR, "Article does not exist");
        validateArticleAccess(oldArticle, loginUser);

        if (StrUtil.isNotBlank(article.getTopic())) {
            oldArticle.setTopic(article.getTopic().trim());
        }
        if (StrUtil.isNotBlank(article.getMainTitle())) {
            oldArticle.setMainTitle(article.getMainTitle().trim());
        }
        if (StrUtil.isNotBlank(article.getSubTitle())) {
            oldArticle.setSubTitle(article.getSubTitle().trim());
        }
        if (article.getContent() != null) {
            oldArticle.setContent(article.getContent());
        }
        if (article.getFullContent() != null) {
            oldArticle.setFullContent(article.getFullContent());
        }
        if (article.getCoverImage() != null) {
            oldArticle.setCoverImage(article.getCoverImage());
        }
        oldArticle.setUpdateTime(LocalDateTime.now());

        int result = articleMapper.update(oldArticle);
        ThrowUtils.throwIf(result <= 0, ErrorCode.OPERATION_ERROR, "Update article failed");
        return true;
    }

    @Override
    public boolean deleteArticle(long id, User loginUser) {
        validateLoginUser(loginUser);
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR, "Article id is invalid");

        Article article = articleMapper.selectOneById(id);
        ThrowUtils.throwIf(article == null, ErrorCode.NOT_FOUND_ERROR, "Article does not exist");
        validateArticleAccess(article, loginUser);

        int result = articleMapper.deleteById(id);
        ThrowUtils.throwIf(result <= 0, ErrorCode.OPERATION_ERROR, "Delete article failed");
        return true;
    }

    @Override
    public void createArticleTaskIfAbsent(String taskId, Long userId, String topic) {
        validateTaskId(taskId);
        if (getByTaskId(taskId) != null) {
            return;
        }
        ThrowUtils.throwIf(StrUtil.isBlank(topic), ErrorCode.PARAMS_ERROR, "Topic cannot be blank");

        LocalDateTime now = LocalDateTime.now();
        Article article = Article.builder()
                .taskId(taskId)
                .userId(userId == null ? DEFAULT_USER_ID : userId)
                .topic(topic.trim())
                .currentStep(ArticleStepEnum.INIT.getValue())
                .status(ArticleStatusEnum.PENDING.getValue())
                .createTime(now)
                .updateTime(now)
                .isDelete(0)
                .build();
        int result = articleMapper.insert(article);
        ThrowUtils.throwIf(result <= 0 || article.getId() == null, ErrorCode.OPERATION_ERROR,
                "Create article task failed");
    }

    @Override
    public void updateArticleStatus(String taskId, ArticleStatusEnum status, String errorMessage) {
        validateTaskId(taskId);
        ThrowUtils.throwIf(status == null, ErrorCode.PARAMS_ERROR, "Article status cannot be null");

        Article article = getByTaskId(taskId);
        ThrowUtils.throwIf(article == null, ErrorCode.NOT_FOUND_ERROR, "Article task does not exist");

        article.setStatus(status.getValue());
        article.setErrorMessage(errorMessage);
        article.setUpdateTime(LocalDateTime.now());
        if (ArticleStatusEnum.COMPLETED.equals(status) || ArticleStatusEnum.FAILED.equals(status)) {
            article.setCompletedTime(LocalDateTime.now());
        }
        int result = articleMapper.update(article);
        ThrowUtils.throwIf(result <= 0, ErrorCode.OPERATION_ERROR, "Update article status failed");
        log.info("Article status updated, taskId={}, status={}", taskId, status.getValue());
    }

    @Override
    public void saveTitleOptionsAndWait(String taskId, ArticleState state, String userRequirement) {
        validateTaskId(taskId);
        ThrowUtils.throwIf(state == null, ErrorCode.PARAMS_ERROR, "Article state cannot be null");
        ThrowUtils.throwIf(state.getTitleOptions() == null || state.getTitleOptions().isEmpty(),
                ErrorCode.PARAMS_ERROR, "Title options cannot be empty");

        Article article = getByTaskId(taskId);
        ThrowUtils.throwIf(article == null, ErrorCode.NOT_FOUND_ERROR, "Article task does not exist");

        article.setTopic(state.getTopic());
        article.setTitleOptions(toJson(state.getTitleOptions()));
        article.setUserRequirement(StrUtil.blankToDefault(userRequirement, null));
        article.setMainTitle(null);
        article.setSubTitle(null);
        article.setCurrentStep(ArticleStepEnum.TITLE_SELECTION.getValue());
        article.setStatus(ArticleStatusEnum.WAITING_USER_INPUT.getValue());
        article.setErrorMessage(null);
        article.setUpdateTime(LocalDateTime.now());

        int result = articleMapper.update(article);
        ThrowUtils.throwIf(result <= 0, ErrorCode.OPERATION_ERROR, "Save title options failed");
        log.info("Article title options saved, taskId={}, count={}", taskId, state.getTitleOptions().size());
    }

    @Override
    public Article prepareTitleRegeneration(String taskId, String additionalRequirement, User loginUser) {
        validateLoginUser(loginUser);
        validateTaskId(taskId);
        ThrowUtils.throwIf(StrUtil.isBlank(additionalRequirement), ErrorCode.PARAMS_ERROR,
                "Additional requirement cannot be blank");

        Article article = getByTaskId(taskId);
        ThrowUtils.throwIf(article == null, ErrorCode.NOT_FOUND_ERROR, "Article task does not exist");
        validateArticleAccess(article, loginUser);
        validateTitleSelectionState(article);

        article.setUserRequirement(mergeRequirement(article.getUserRequirement(), additionalRequirement));
        article.setCurrentStep(ArticleStepEnum.TITLE.getValue());
        article.setStatus(ArticleStatusEnum.PROCESSING.getValue());
        article.setErrorMessage(null);
        article.setUpdateTime(LocalDateTime.now());

        int result = articleMapper.update(article);
        ThrowUtils.throwIf(result <= 0, ErrorCode.OPERATION_ERROR, "Prepare title regeneration failed");
        log.info("Article title regeneration prepared, taskId={}", taskId);
        return article;
    }

    @Override
    public TitleResult selectTitleOption(String taskId, Integer titleIndex, User loginUser) {
        validateLoginUser(loginUser);
        validateTaskId(taskId);
        ThrowUtils.throwIf(titleIndex == null || titleIndex < 0, ErrorCode.PARAMS_ERROR,
                "Title index is invalid");

        Article article = getByTaskId(taskId);
        ThrowUtils.throwIf(article == null, ErrorCode.NOT_FOUND_ERROR, "Article task does not exist");
        validateArticleAccess(article, loginUser);
        validateTitleSelectionState(article);

        List<TitleResult> titleOptions = parseTitleOptions(article.getTitleOptions());
        ThrowUtils.throwIf(titleIndex >= titleOptions.size(), ErrorCode.PARAMS_ERROR,
                "Title index is out of range");

        TitleResult selectedTitle = titleOptions.get(titleIndex);
        ThrowUtils.throwIf(selectedTitle == null || StrUtil.isBlank(selectedTitle.getMainTitle()),
                ErrorCode.PARAMS_ERROR, "Selected title is invalid");

        article.setMainTitle(selectedTitle.getMainTitle().trim());
        article.setSubTitle(StrUtil.blankToDefault(selectedTitle.getSubTitle(), null));
        article.setCurrentStep(ArticleStepEnum.OUTLINE.getValue());
        article.setStatus(ArticleStatusEnum.PROCESSING.getValue());
        article.setErrorMessage(null);
        article.setUpdateTime(LocalDateTime.now());

        int result = articleMapper.update(article);
        ThrowUtils.throwIf(result <= 0, ErrorCode.OPERATION_ERROR, "Select title option failed");
        log.info("Article title selected, taskId={}, index={}", taskId, titleIndex);
        return selectedTitle;
    }

    @Override
    public void saveArticleContent(String taskId, ArticleState state) {
        validateTaskId(taskId);
        ThrowUtils.throwIf(state == null, ErrorCode.PARAMS_ERROR, "Article state cannot be null");

        Article article = getByTaskId(taskId);
        ThrowUtils.throwIf(article == null, ErrorCode.NOT_FOUND_ERROR, "Article task does not exist");

        TitleResult title = state.getSelectedTitle() == null ? state.getTitle() : state.getSelectedTitle();
        if (title != null) {
            article.setMainTitle(title.getMainTitle());
            article.setSubTitle(title.getSubTitle());
        }
        article.setTopic(state.getTopic());
        article.setOutline(toJson(state.getOutlineMarkdown()));
        article.setContent(state.getContent());
        article.setFullContent(state.getFullContent());
        article.setCoverImage(resolveCoverImage(state));
        article.setImages(toJson(state.getImages()));
        article.setCurrentStep(ArticleStepEnum.COMPLETED.getValue());
        article.setStatus(ArticleStatusEnum.COMPLETED.getValue());
        article.setErrorMessage(null);
        article.setCompletedTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());

        int result = articleMapper.update(article);
        ThrowUtils.throwIf(result <= 0, ErrorCode.OPERATION_ERROR, "Save article content failed");
        log.info("Article content saved, taskId={}", taskId);
    }

    private void validateTaskId(String taskId) {
        ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR, "Task id cannot be blank");
    }

    private void validateLoginUser(User loginUser) {
        ThrowUtils.throwIf(loginUser == null || loginUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
    }

    private void validateArticleAccess(Article article, User loginUser) {
        if (isAdmin(loginUser)) {
            return;
        }
        ThrowUtils.throwIf(article.getUserId() == null || !article.getUserId().equals(loginUser.getId()),
                ErrorCode.NO_AUTH_ERROR);
    }

    private boolean isAdmin(User loginUser) {
        return loginUser != null && UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
    }

    private void validateTitleSelectionState(Article article) {
        ThrowUtils.throwIf(!ArticleStatusEnum.WAITING_USER_INPUT.getValue().equals(article.getStatus())
                        || !ArticleStepEnum.TITLE_SELECTION.getValue().equals(article.getCurrentStep()),
                ErrorCode.OPERATION_ERROR, "Article task is not waiting for title selection");
    }

    private String mergeRequirement(String currentRequirement, String additionalRequirement) {
        String trimmedAdditionalRequirement = additionalRequirement.trim();
        if (StrUtil.isBlank(currentRequirement)) {
            return trimmedAdditionalRequirement;
        }
        return currentRequirement.trim() + "\n" + trimmedAdditionalRequirement;
    }

    private List<TitleResult> parseTitleOptions(String titleOptionsJson) {
        ThrowUtils.throwIf(StrUtil.isBlank(titleOptionsJson), ErrorCode.OPERATION_ERROR,
                "Title options do not exist");
        try {
            return objectMapper.readValue(titleOptionsJson, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Parse title options failed");
        }
    }

    private String resolveCoverImage(ArticleState state) {
        if (StrUtil.isNotBlank(state.getCoverImage())) {
            return state.getCoverImage();
        }
        if (state.getImages() == null || state.getImages().isEmpty()) {
            return null;
        }
        return state.getImages().stream()
                .filter(image -> image.getPosition() != null && image.getPosition() == 1)
                .map(ImageResult::getUrl)
                .filter(StrUtil::isNotBlank)
                .findFirst()
                .orElse(null);
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Serialize article content failed");
        }
    }
}
