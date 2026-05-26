package cn.nuist.aiarticlewriter.service.impl;

import cn.nuist.aiarticlewriter.model.enums.ImageMediaTypeEnum;
import cn.nuist.aiarticlewriter.model.enums.ImageMethodEnum;
import cn.nuist.aiarticlewriter.model.image.ImageAsset;
import cn.nuist.aiarticlewriter.model.image.ImageRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Slf4j
class AiImageGenerationServiceImplIntegrationTest {

    @Autowired
    private AiImageGenerationServiceImpl aiImageGenerationService;

    @Value("${ai-image.api-key:}")
    private String apiKey;

    @Value("${ai-image.integration-test.enabled:false}")
    private boolean integrationTestEnabled;

    @Test
    @Timeout(180)
    void shouldGenerateImageWithRealDashScopeApi() {
        Assumptions.assumeTrue(integrationTestEnabled, "AI image generation integration test is disabled");
        Assumptions.assumeTrue(apiKey != null && !apiKey.isBlank(), "DashScope API key is not configured");

        ImageRequest request = ImageRequest.builder()
                .position(5)
                .type("cover")
                .sectionTitle("AI Article Writing")
                .prompt("Create a clean 16:9 editorial illustration about AI article writing, with a laptop, structured outline cards, and subtle technology elements.")
                .visualType("ai generated illustration")
                .style("modern editorial illustration")
                .build();

        ImageAsset asset = aiImageGenerationService.acquire(request);

        log.info("Generated AI image asset, fileName={}, url={}",
                asset == null ? null : asset.getFileName(),
                asset == null ? null : asset.getUrl());

        assertThat(asset).isNotNull();
        assertThat(asset.getMethod()).isEqualTo(ImageMethodEnum.AI_GENERATION);
        assertThat(asset.getMediaType()).isEqualTo(ImageMediaTypeEnum.IMAGE_URL);
        assertThat(asset.getContentType()).isEqualTo("image/png");
        assertThat(asset.getUrl()).startsWith("https://");
    }
}
