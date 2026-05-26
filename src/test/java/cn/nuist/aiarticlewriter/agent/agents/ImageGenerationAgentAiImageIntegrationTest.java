package cn.nuist.aiarticlewriter.agent.agents;

import cn.nuist.aiarticlewriter.model.enums.ImageMethodEnum;
import cn.nuist.aiarticlewriter.model.state.article.ImageRequirement;
import cn.nuist.aiarticlewriter.model.state.article.ImageResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end integration test for AI image generation and COS storage.
 */
@SpringBootTest(properties = "image-storage.cos.enabled=true")
@Slf4j
class ImageGenerationAgentAiImageIntegrationTest {

    @Autowired
    private ImageGenerationAgent imageGenerationAgent;

    @Value("${ai-image.api-key:}")
    private String aiImageApiKey;

    @Value("${image-storage.cos.secret-id:}")
    private String cosSecretId;

    @Value("${image-storage.cos.secret-key:}")
    private String cosSecretKey;

    @Value("${image-storage.cos.region:}")
    private String cosRegion;

    @Value("${image-storage.cos.bucket-name:}")
    private String cosBucketName;

    @Value("${ai-image.full-chain-test.enabled:false}")
    private boolean fullChainTestEnabled;

    @Test
    @Timeout(240)
    void shouldGenerateAiImageAndUploadToCos() {
        Assumptions.assumeTrue(fullChainTestEnabled, "AI image full-chain integration test is disabled");
        Assumptions.assumeTrue(aiImageApiKey != null && !aiImageApiKey.isBlank(),
                "DashScope API key is not configured");
        Assumptions.assumeTrue(cosSecretId != null && !cosSecretId.isBlank(),
                "COS secret ID is not configured");
        Assumptions.assumeTrue(cosSecretKey != null && !cosSecretKey.isBlank(),
                "COS secret key is not configured");
        Assumptions.assumeTrue(cosRegion != null && !cosRegion.isBlank(),
                "COS region is not configured");
        Assumptions.assumeTrue(cosBucketName != null && !cosBucketName.isBlank(),
                "COS bucket name is not configured");

        ImageRequirement requirement = ImageRequirement.builder()
                .position(6)
                .type("cover")
                .sectionTitle("AI Article Writing Workflow")
                .keywords("AI article writing workflow")
                .prompt("Create a clean 16:9 editorial illustration about AI article writing, with a laptop, outline cards, image cards, and a final article preview. No visible text.")
                .visualType("ai generated illustration")
                .aspectRatio("16:9")
                .style("modern editorial illustration")
                .preferredMethod(ImageMethodEnum.AI_GENERATION.getValue())
                .build();

        List<ImageResult> results = imageGenerationAgent.generateImages(List.of(requirement));

        assertThat(results).hasSize(1);
        ImageResult result = results.get(0);
        log.info("Generated full-chain AI image result, method={}, mediaType={}, url={}",
                result.getMethod(), result.getMediaType(), result.getUrl());

        assertThat(result.getMethod()).isEqualTo(ImageMethodEnum.AI_GENERATION.getValue());
        assertThat(result.getUrl()).isNotBlank();
        assertThat(result.getUrl()).startsWith("https://");
        assertThat(result.getUrl()).contains("article-images/ai-generated/6.png");
        assertThat(result.getUrl()).doesNotContain("dashscope");
        assertThat(result.getUrl()).doesNotContain("picsum.photos");
    }
}
