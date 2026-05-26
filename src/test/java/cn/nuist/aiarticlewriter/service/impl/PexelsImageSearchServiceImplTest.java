package cn.nuist.aiarticlewriter.service.impl;

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
class PexelsImageSearchServiceImplTest {

    @Autowired
    private PexelsImageSearchServiceImpl imageSearchService;

    @Autowired
    private PicsumImageServiceImpl picsumImageService;

    @Value("${image-search.pexels.api-key:}")
    private String pexelsApiKey;

    @Test
    void shouldReturnPexelsMethod() {
        assertThat(imageSearchService.getMethod()).isEqualTo(ImageMethodEnum.PEXELS);
    }

    @Test
    void shouldNotSupportBlankKeywords() {
        ImageRequest request = ImageRequest.builder()
                .keywords(" ")
                .build();

        assertThat(imageSearchService.supports(request)).isFalse();
    }

    @Test
    void shouldReturnStablePicsumFallbackImage() {
        ImageRequest request = ImageRequest.builder()
                .position(2)
                .build();

        assertThat(picsumImageService.acquire(request).getUrl())
                .isEqualTo("https://picsum.photos/800/600?random=2");
    }

    @Test
    void shouldNormalizeInvalidFallbackPosition() {
        ImageRequest request = ImageRequest.builder()
                .position(0)
                .build();

        assertThat(picsumImageService.acquire(request).getUrl())
                .isEqualTo("https://picsum.photos/800/600?random=1");
    }

    @Test
    @Timeout(60)
    void shouldSearchImageWithRealPexelsApi() {
        Assumptions.assumeTrue(pexelsApiKey != null && !pexelsApiKey.isBlank(),
                "Pexels API key is not configured");

        String keywords = "AI article writing workflow";
        ImageRequest request = ImageRequest.builder()
                .position(1)
                .keywords(keywords)
                .sectionTitle("AI article writing workflow")
                .type("cover")
                .build();
        ImageAsset imageAsset = imageSearchService.acquire(request);

        log.info("Pexels image search result, keywords={}, imageUrl={}", keywords, imageAsset.getUrl());

        assertThat(imageAsset.getUrl()).isNotBlank();
        assertThat(imageAsset.getUrl()).startsWith("https://");
    }
}
