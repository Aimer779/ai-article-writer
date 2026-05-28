package cn.nuist.aiarticlewriter.model.dto.article;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;

/**
 * Request for regenerating title options with extra user requirement.
 */
@Data
public class ArticleTitleRegenerateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Additional writing direction supplied by the user.
     */
    private String additionalRequirement;
}
