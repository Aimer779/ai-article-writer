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
}
