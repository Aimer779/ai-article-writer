package cn.nuist.aiarticlewriter.model.dto.article;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * Request for confirming an article outline before content generation.
 */
@Data
public class ArticleOutlineConfirmRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Confirmed outline Markdown.
     */
    private String outlineMarkdown;
}
