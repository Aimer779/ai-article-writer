package cn.nuist.aiarticlewriter.model.state.article;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * Image requirement generated from article content.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageRequirement implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Image position in the article. Position 1 is the cover image.
     */
    private Integer position;

    /**
     * Image type, such as cover, illustration, or diagram.
     */
    private String type;

    /**
     * Related article section title.
     */
    private String sectionTitle;

    /**
     * Image search or generation keywords.
     */
    private String keywords;

    /**
     * Preferred image source, such as PEXELS, AI_GENERATION, MERMAID, MEME, or SVG_DIAGRAM.
     */
    private String imageSource;

    /**
     * Image generation prompt.
     */
    private String prompt;

    /**
     * Placeholder ID used to locate the image insertion point in article content.
     */
    private String placeholderId;

    /**
     * Visual intent, such as photo, flowchart, diagram, or meme.
     */
    private String visualType;

    /**
     * Image aspect ratio.
     */
    private String aspectRatio;

    /**
     * Image style.
     */
    private String style;

    /**
     * Preferred image acquisition method.
     */
    private String preferredMethod;
}
