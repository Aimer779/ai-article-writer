package cn.nuist.aiarticlewriter.service.impl;

import cn.nuist.aiarticlewriter.model.enums.ImageMethodEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PexelsImageSearchServiceImplTest {

    private final PexelsImageSearchServiceImpl imageSearchService = new PexelsImageSearchServiceImpl(new ObjectMapper());

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
}
