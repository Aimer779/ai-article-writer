package cn.nuist.aiarticlewriter.config;

import cn.nuist.aiarticlewriter.constant.ArticleConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * SVG conceptual diagram generation configuration properties.
 */
@Data
@Component
@ConfigurationProperties(prefix = "svg-diagram")
public class SvgDiagramProperties {

    /**
     * Default SVG width.
     */
    private Integer defaultWidth = ArticleConstant.SVG_DEFAULT_WIDTH;

    /**
     * Default SVG height.
     */
    private Integer defaultHeight = ArticleConstant.SVG_DEFAULT_HEIGHT;

    /**
     * Suggested COS storage folder.
     */
    private String folder = "svg-diagrams";
}
