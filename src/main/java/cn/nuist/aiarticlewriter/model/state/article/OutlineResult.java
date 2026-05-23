package cn.nuist.aiarticlewriter.model.state.article;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Structured article outline result.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutlineResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Ordered outline sections.
     */
    private List<OutlineSection> sections;
}
