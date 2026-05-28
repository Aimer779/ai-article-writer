package cn.nuist.aiarticlewriter.model.dto.article;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * Request for selecting a generated title option.
 */
@Data
public class ArticleTitleSelectRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Zero-based title option index.
     */
    private Integer titleIndex;
}
