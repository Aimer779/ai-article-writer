package cn.nuist.aiarticlewriter.model.state.article;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Single section in an article outline.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutlineSection implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Section number.
     */
    private Integer section;

    /**
     * Section title.
     */
    private String title;

    /**
     * Key points covered by the section.
     */
    private List<String> points;
}
