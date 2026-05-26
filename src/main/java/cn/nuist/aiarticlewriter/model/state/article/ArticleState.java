package cn.nuist.aiarticlewriter.model.state.article;

import cn.nuist.aiarticlewriter.model.enums.ArticleStatusEnum;
import cn.nuist.aiarticlewriter.model.enums.ArticleStepEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Runtime state shared by the serial article generation agents.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleState implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Article generation task ID.
     */
    private String taskId;

    /**
     * User ID.
     */
    private Long userId;

    /**
     * Article topic.
     */
    private String topic;

    /**
     * Candidate title results from the title agent.
     */
    private List<TitleResult> titleOptions;

    /**
     * Selected title result from user choice.
     */
    private TitleResult selectedTitle;

    /**
     * Legacy title field. Prefer selectedTitle for new workflow code.
     */
    private TitleResult title;

    /**
     * Outline result from the outline agent.
     */
    private OutlineResult outline;

    /**
     * Article outline in Markdown format.
     */
    private String outlineMarkdown;

    /**
     * Article content in Markdown format.
     */
    private String content;

    /**
     * Article content with image placeholders from the image requirement agent.
     */
    private String contentWithPlaceholders;

    /**
     * Image requirements from the image requirement agent.
     */
    private List<ImageRequirement> imageRequirements;

    /**
     * Cover image URL.
     */
    private String coverImage;

    /**
     * Image results from the image generation agent.
     */
    private List<ImageResult> images;

    /**
     * Full Markdown content assembled with images.
     */
    private String fullContent;

    /**
     * Current workflow step.
     */
    private ArticleStepEnum currentStep;

    /**
     * Current workflow status.
     */
    private ArticleStatusEnum status;

    /**
     * Error message when the workflow fails.
     */
    private String errorMessage;
}
