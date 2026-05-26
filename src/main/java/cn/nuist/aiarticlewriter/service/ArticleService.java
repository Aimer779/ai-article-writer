package cn.nuist.aiarticlewriter.service;

import cn.nuist.aiarticlewriter.model.enums.ArticleStatusEnum;
import cn.nuist.aiarticlewriter.model.state.article.ArticleState;

/**
 * Article persistence service.
 */
public interface ArticleService {

    /**
     * Create an article task when it does not exist.
     *
     * @param taskId article generation task id
     * @param userId user id
     * @param topic article topic
     */
    void createArticleTaskIfAbsent(String taskId, Long userId, String topic);

    /**
     * Update article task status.
     *
     * @param taskId article generation task id
     * @param status article status
     * @param errorMessage error message
     */
    void updateArticleStatus(String taskId, ArticleStatusEnum status, String errorMessage);

    /**
     * Save generated article content.
     *
     * @param taskId article generation task id
     * @param state article generation state
     */
    void saveArticleContent(String taskId, ArticleState state);
}
