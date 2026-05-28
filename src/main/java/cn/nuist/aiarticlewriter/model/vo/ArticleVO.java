package cn.nuist.aiarticlewriter.model.vo;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * Article view object.
 */
@Data
public class ArticleVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Article id.
     */
    private Long id;

    /**
     * Article generation task id.
     */
    private String taskId;

    /**
     * User id.
     */
    private Long userId;

    /**
     * Article topic.
     */
    private String topic;

    /**
     * Main title.
     */
    private String mainTitle;

    /**
     * Subtitle.
     */
    private String subTitle;

    /**
     * Candidate title options in JSON format.
     */
    private String titleOptions;

    /**
     * User writing requirement accumulated during creation.
     */
    private String userRequirement;

    /**
     * Current generation workflow step.
     */
    private String currentStep;

    /**
     * Article outline in JSON format.
     */
    private String outline;

    /**
     * Article content in Markdown format.
     */
    private String content;

    /**
     * Full article content with images in Markdown format.
     */
    private String fullContent;

    /**
     * Cover image URL.
     */
    private String coverImage;

    /**
     * Article image list in JSON format.
     */
    private String images;

    /**
     * Article generation status.
     */
    private String status;

    /**
     * Error message.
     */
    private String errorMessage;

    /**
     * Create time.
     */
    private LocalDateTime createTime;

    /**
     * Task completion time.
     */
    private LocalDateTime completedTime;

    /**
     * Update time.
     */
    private LocalDateTime updateTime;
}
