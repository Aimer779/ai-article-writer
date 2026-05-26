package cn.nuist.aiarticlewriter.model.dto.article;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * Article update request.
 */
@Data
public class ArticleUpdateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Article id.
     */
    private Long id;

    /**
     * Article topic.
     */
    private String topic;

    /**
     * Main title.
     */
    private String mainTitle;

    /**
     * Subtitle.
     */
    private String subTitle;

    /**
     * Article content in Markdown format.
     */
    private String content;

    /**
     * Full article content with images in Markdown format.
     */
    private String fullContent;

    /**
     * Cover image URL.
     */
    private String coverImage;
}
