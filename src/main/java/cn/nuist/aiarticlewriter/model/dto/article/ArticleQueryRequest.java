package cn.nuist.aiarticlewriter.model.dto.article;

import cn.nuist.aiarticlewriter.common.PageRequest;
import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Article query request.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ArticleQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Article id.
     */
    private Long id;

    /**
     * Article generation task id.
     */
    private String taskId;

    /**
     * User id.
     */
    private Long userId;

    /**
     * Article topic.
     */
    private String topic;

    /**
     * Main title.
     */
    private String mainTitle;

    /**
     * Article status.
     */
    private String status;
}
