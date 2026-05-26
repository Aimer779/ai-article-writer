package cn.nuist.aiarticlewriter.service;

import cn.nuist.aiarticlewriter.model.enums.ArticleStatusEnum;
import cn.nuist.aiarticlewriter.model.entity.Article;
import cn.nuist.aiarticlewriter.model.entity.User;
import cn.nuist.aiarticlewriter.model.state.article.ArticleState;
import cn.nuist.aiarticlewriter.model.vo.ArticleVO;
import com.mybatisflex.core.paginate.Page;

/**
 * Article persistence service.
 */
public interface ArticleService {

    /**
     * Create an article generation task.
     *
     * @param topic article topic
     * @param loginUser current login user
     * @return article generation task id
     */
    String createArticleTask(String topic, User loginUser);

    /**
     * Convert article entity to view object.
     *
     * @param article article entity
     * @return article view object
     */
    ArticleVO getArticleVO(Article article);

    /**
     * Get article view object by id.
     *
     * @param id article id
     * @param loginUser current login user
     * @return article view object
     */
    ArticleVO getArticleVOById(long id, User loginUser);

    /**
     * Get article view object by task id.
     *
     * @param taskId article generation task id
     * @param loginUser current login user
     * @return article view object
     */
    ArticleVO getArticleVOByTaskId(String taskId, User loginUser);

    /**
     * Get article by task id.
     *
     * @param taskId article generation task id
     * @return article entity
     */
    Article getByTaskId(String taskId);

    /**
     * Page article view objects.
     *
     * @param pageNumber page number
     * @param pageSize page size
     * @param article article query condition
     * @param sortField sort field
     * @param sortOrder sort order
     * @param loginUser current login user
     * @return article view object page
     */
    Page<ArticleVO> pageArticleVO(long pageNumber, long pageSize, Article article, String sortField,
            String sortOrder, User loginUser);

    /**
     * Update article.
     *
     * @param article article update data
     * @param loginUser current login user
     * @return update result
     */
    boolean updateArticle(Article article, User loginUser);

    /**
     * Delete article.
     *
     * @param id article id
     * @param loginUser current login user
     * @return delete result
     */
    boolean deleteArticle(long id, User loginUser);

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
