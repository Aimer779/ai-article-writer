package cn.nuist.aiarticlewriter.service.impl;

import cn.nuist.aiarticlewriter.exception.BusinessException;
import cn.nuist.aiarticlewriter.exception.ErrorCode;
import cn.nuist.aiarticlewriter.exception.ThrowUtils;
import cn.nuist.aiarticlewriter.mapper.ArticleMapper;
import cn.nuist.aiarticlewriter.model.entity.Article;
import cn.nuist.aiarticlewriter.model.enums.ArticleStatusEnum;
import cn.nuist.aiarticlewriter.model.state.article.ArticleState;
import cn.nuist.aiarticlewriter.model.state.article.TitleResult;
import cn.nuist.aiarticlewriter.service.ArticleService;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

/**
 * Article persistence service implementation.
 */
@Service
public class ArticleServiceImpl implements ArticleService {

    private static final long DEFAULT_USER_ID = 0L;

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void createArticleTaskIfAbsent(String taskId, Long userId, String topic) {
        validateTaskId(taskId);
        if (findByTaskId(taskId) != null) {
            return;
        }
        ThrowUtils.throwIf(StrUtil.isBlank(topic), ErrorCode.PARAMS_ERROR, "Topic cannot be blank");

        LocalDateTime now = LocalDateTime.now();
        Article article = Article.builder()
                .taskId(taskId)
                .userId(userId == null ? DEFAULT_USER_ID : userId)
                .topic(topic.trim())
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

        Article article = findByTaskId(taskId);
        ThrowUtils.throwIf(article == null, ErrorCode.NOT_FOUND_ERROR, "Article task does not exist");

        article.setStatus(status.getValue());
        article.setErrorMessage(errorMessage);
        article.setUpdateTime(LocalDateTime.now());
        if (ArticleStatusEnum.COMPLETED.equals(status) || ArticleStatusEnum.FAILED.equals(status)) {
            article.setCompletedTime(LocalDateTime.now());
        }
        int result = articleMapper.update(article);
        ThrowUtils.throwIf(result <= 0, ErrorCode.OPERATION_ERROR, "Update article status failed");
    }

    @Override
    public void saveArticleContent(String taskId, ArticleState state) {
        validateTaskId(taskId);
        ThrowUtils.throwIf(state == null, ErrorCode.PARAMS_ERROR, "Article state cannot be null");

        Article article = findByTaskId(taskId);
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
        article.setCoverImage(state.getCoverImage());
        article.setImages(toJson(state.getImages()));
        article.setStatus(ArticleStatusEnum.COMPLETED.getValue());
        article.setErrorMessage(null);
        article.setCompletedTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());

        int result = articleMapper.update(article);
        ThrowUtils.throwIf(result <= 0, ErrorCode.OPERATION_ERROR, "Save article content failed");
    }

    private Article findByTaskId(String taskId) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("taskId", taskId);
        return articleMapper.selectOneByQuery(queryWrapper);
    }

    private void validateTaskId(String taskId) {
        ThrowUtils.throwIf(StrUtil.isBlank(taskId), ErrorCode.PARAMS_ERROR, "Task id cannot be blank");
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
