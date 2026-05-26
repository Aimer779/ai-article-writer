package cn.nuist.aiarticlewriter.model.state.article;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Image analysis result from agent 4.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Agent4Result implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Article content with image placeholders.
     */
    private String contentWithPlaceholders;

    /**
     * Image requirements matched by placeholder ID.
     */
    private List<ImageRequirement> imageRequirements;
}
