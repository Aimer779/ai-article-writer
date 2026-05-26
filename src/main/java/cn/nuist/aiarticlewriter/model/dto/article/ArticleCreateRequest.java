package cn.nuist.aiarticlewriter.model.dto.article;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * Article creation request.
 */
@Data
public class ArticleCreateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Article topic.
     */
    private String topic;
}
