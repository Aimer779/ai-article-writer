package cn.nuist.aiarticlewriter.model.image;

import cn.nuist.aiarticlewriter.model.enums.ImageMethodEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * Unified request for article image acquisition.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Image position in the article. Position 1 is the cover image.
     */
    private Integer position;

    /**
     * Image type, such as cover or section.
     */
    private String type;

    /**
     * Related article section title.
     */
    private String sectionTitle;

    /**
     * Image search keywords.
     */
    private String keywords;

    /**
     * Image generation prompt.
     */
    private String prompt;

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
    private ImageMethodEnum preferredMethod;
}
