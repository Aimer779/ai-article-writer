package cn.nuist.aiarticlewriter.service.impl;

import cn.nuist.aiarticlewriter.agent.support.ArticleLlmClient;
import cn.nuist.aiarticlewriter.config.SvgDiagramProperties;
import cn.nuist.aiarticlewriter.model.enums.ImageMediaTypeEnum;
import cn.nuist.aiarticlewriter.model.enums.ImageMethodEnum;
import cn.nuist.aiarticlewriter.model.image.ImageAsset;
import cn.nuist.aiarticlewriter.model.image.ImageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SvgDiagramImageServiceImplTest {

    private ArticleLlmClient articleLlmClient;

    private SvgDiagramImageServiceImpl svgDiagramImageService;

    @BeforeEach
    void setUp() {
        SvgDiagramProperties properties = new SvgDiagramProperties();
        properties.setDefaultWidth(800);
        properties.setDefaultHeight(600);
        properties.setFolder("svg-diagrams");

        articleLlmClient = mock(ArticleLlmClient.class);
        svgDiagramImageService = new SvgDiagramImageServiceImpl(properties, articleLlmClient);
    }

    @Test
    void shouldGenerateSanitizedSvgAsset() {
        when(articleLlmClient.renderPrompt(anyString(), anyMap())).thenReturn("rendered prompt");
        when(articleLlmClient.callLlm("rendered prompt")).thenReturn("""
                ```svg
                <svg width="800" height="600" viewBox="0 0 800 600" onclick="alert(1)">
                  <script>alert(1)</script>
                  <defs>
                    <marker id="arrow" markerWidth="10" markerHeight="10" refX="8" refY="3" orient="auto">
                      <path d="M0,0 L0,6 L9,3 z" fill="#4A90D9"/>
                    </marker>
                  </defs>
                  <rect id="core-node" x="280" y="220" width="240" height="80" rx="12" fill="#E8F4FC"/>
                  <text x="400" y="265" text-anchor="middle" font-family="Arial, sans-serif" font-size="18">
                    AI Workflow
                  </text>
                  <line x1="160" y1="260" x2="280" y2="260" stroke="#4A90D9" stroke-width="3" marker-end="url(#arrow)"/>
                </svg>
                ```
                """);

        ImageRequest request = ImageRequest.builder()
                .position(2)
                .sectionTitle("Workflow Overview")
                .prompt("Create a diagram about an AI article writing workflow")
                .build();

        ImageAsset asset = svgDiagramImageService.acquire(request);

        assertThat(asset).isNotNull();
        assertThat(asset.getMethod()).isEqualTo(ImageMethodEnum.SVG_DIAGRAM);
        assertThat(asset.getMediaType()).isEqualTo(ImageMediaTypeEnum.SVG);
        assertThat(asset.getContentType()).isEqualTo("image/svg+xml");
        assertThat(asset.getFileName()).isEqualTo("svg-diagram-2.svg");
        assertThat(asset.getStorageFolder()).isEqualTo("svg-diagrams");
        assertThat(asset.getBytes()).isNotEmpty();
        assertThat(asset.getContent())
                .startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .contains("<svg")
                .contains("AI Workflow")
                .doesNotContain("<script")
                .doesNotContain("onclick=");
    }

    @Test
    void shouldNotSupportBlankRequirement() {
        ImageRequest request = ImageRequest.builder()
                .prompt(" ")
                .keywords(" ")
                .build();

        assertThat(svgDiagramImageService.supports(request)).isFalse();
    }

    @Test
    void shouldReturnNullWhenLlmReturnsInvalidSvg() {
        when(articleLlmClient.renderPrompt(anyString(), anyMap())).thenReturn("rendered prompt");
        when(articleLlmClient.callLlm("rendered prompt")).thenReturn("not svg");

        ImageRequest request = ImageRequest.builder()
                .position(1)
                .prompt("Create a concept diagram")
                .build();

        assertThat(svgDiagramImageService.acquire(request)).isNull();
    }
}
