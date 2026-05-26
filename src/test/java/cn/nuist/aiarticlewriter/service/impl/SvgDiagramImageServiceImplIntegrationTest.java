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
class SvgDiagramImageServiceImplIntegrationTest {

    @Autowired
    private SvgDiagramImageServiceImpl svgDiagramImageService;

    @Value("${langchain4j.open-ai.chat-model.api-key:}")
    private String apiKey;

    @Value("${svg-diagram.integration-test.enabled:false}")
    private boolean integrationTestEnabled;

    @Test
    @Timeout(240)
    void shouldGenerateSvgWithRealLlm() {
        Assumptions.assumeTrue(integrationTestEnabled, "SVG diagram LLM integration test is disabled");
        Assumptions.assumeTrue(apiKey != null && !apiKey.isBlank(), "AI model API key is not configured");

        ImageRequest request = ImageRequest.builder()
                .position(3)
                .type("section")
                .sectionTitle("AI Article Workflow")
                .prompt("Create a concise conceptual diagram showing Topic Input, Outline Planning, Draft Writing, Image Planning, and Final Article.")
                .visualType("svg_diagram")
                .style("clean technical documentation")
                .build();

        ImageAsset asset = svgDiagramImageService.acquire(request);

        log.info("Generated SVG diagram asset, fileName={}, size={} bytes",
                asset == null ? null : asset.getFileName(),
                asset == null || asset.getBytes() == null ? 0 : asset.getBytes().length);

        assertThat(asset).isNotNull();
        assertThat(asset.getMethod()).isEqualTo(ImageMethodEnum.SVG_DIAGRAM);
        assertThat(asset.getMediaType()).isEqualTo(ImageMediaTypeEnum.SVG);
        assertThat(asset.getContentType()).isEqualTo("image/svg+xml");
        assertThat(asset.getContent()).contains("<svg");
        assertThat(asset.getContent()).contains("</svg>");
        assertThat(asset.getContent()).doesNotContain("<script");
        assertThat(asset.getContent()).doesNotContain("foreignObject");
        assertThat(asset.getBytes()).isNotEmpty();
    }
}
