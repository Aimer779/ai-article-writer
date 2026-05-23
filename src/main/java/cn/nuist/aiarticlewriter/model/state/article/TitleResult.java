package cn.nuist.aiarticlewriter.model.state.article;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * Structured title generation result.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TitleResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Main article title.
     */
    private String mainTitle;

    /**
     * Article subtitle.
     */
    private String subTitle;

}
