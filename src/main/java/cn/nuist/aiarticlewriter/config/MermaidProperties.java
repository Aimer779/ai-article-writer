package cn.nuist.aiarticlewriter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Mermaid CLI rendering configuration properties.
 */
@Data
@Component
@ConfigurationProperties(prefix = "mermaid")
public class MermaidProperties {

    /**
     * CLI command. Use mmdc.cmd on Windows and mmdc on Linux or macOS.
     */
    private String cliCommand = "mmdc";

    /**
     * Background color. Use transparent for transparent background.
     */
    private String backgroundColor = "transparent";

    /**
     * Output format: svg, png, or pdf.
     */
    private String outputFormat = "svg";

    /**
     * Image width in pixels.
     */
    private Integer width = 1200;

    /**
     * Command execution timeout in milliseconds.
     */
    private Long timeout = 30000L;
}
