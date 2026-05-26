package cn.nuist.aiarticlewriter.service.impl;

import cn.nuist.aiarticlewriter.model.enums.ImageMethodEnum;
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
class PexelsImageSearchServiceImplTest {

    @Autowired
    private PexelsImageSearchServiceImpl imageSearchService;

    @Value("${image-search.pexels.api-key:}")
    private String pexelsApiKey;

    @Test
    void shouldReturnPexelsMethod() {
        assertThat(imageSearchService.getMethod()).isEqualTo(ImageMethodEnum.PEXELS);
    }

    @Test
    void shouldReturnNullWhenKeywordsAreBlank() {
        assertThat(imageSearchService.searchImage(" ")).isNull();
    }

    @Test
    void shouldReturnStablePicsumFallbackImage() {
        assertThat(imageSearchService.getFallbackImage(2))
                .isEqualTo("https://picsum.photos/seed/ai-article-2/1200/800");
    }

    @Test
    void shouldNormalizeInvalidFallbackPosition() {
        assertThat(imageSearchService.getFallbackImage(0))
                .isEqualTo("https://picsum.photos/seed/ai-article-1/1200/800");
    }

    @Test
    @Timeout(60)
    void shouldSearchImageWithRealPexelsApi() {
        Assumptions.assumeTrue(pexelsApiKey != null && !pexelsApiKey.isBlank(),
                "Pexels API key is not configured");

        String keywords = "AI article writing workflow";
        String imageUrl = imageSearchService.searchImage(keywords);

        log.info("Pexels image search result, keywords={}, imageUrl={}", keywords, imageUrl);

        assertThat(imageUrl).isNotBlank();
        assertThat(imageUrl).startsWith("https://");
    }
}
