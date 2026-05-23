package cn.nuist.aiarticlewriter.model.state.article;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * Image generation or selection result.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Image position in the article. Position 1 is the cover image.
     */
    private Integer position;

    /**
     * Image URL.
     */
    private String url;

    /**
     * Image acquisition method, such as generated, searched, or uploaded.
     */
    private String method;

    /**
     * Image search or generation keywords.
     */
    private String keywords;

    /**
     * Related article section title.
     */
    private String sectionTitle;

    /**
     * Image description.
     */
    private String description;
}
