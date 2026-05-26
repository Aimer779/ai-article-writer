package cn.nuist.aiarticlewriter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI image generation configuration properties.
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai-image")
public class AiImageProperties {

    /**
     * DashScope API key.
     */
    private String apiKey;

    /**
     * DashScope image generation endpoint.
     */
    private String baseUrl = "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation";

    /**
     * Qwen image generation model.
     */
    private String model = "qwen-image-2.0";

    /**
     * Generated image size.
     */
    private String size = "1664*928";

    /**
     * Negative prompt for image quality control.
     */
    private String negativePrompt = "low quality, blurry, distorted, bad anatomy, messy composition, watermark";

    /**
     * Whether DashScope should extend the prompt.
     */
    private Boolean promptExtend = true;

    /**
     * Whether to add model watermark.
     */
    private Boolean watermark = false;

    /**
     * HTTP request timeout in milliseconds.
     */
    private Integer timeout = 60000;

    /**
     * Suggested COS storage folder.
     */
    private String folder = "ai-generated";
}
