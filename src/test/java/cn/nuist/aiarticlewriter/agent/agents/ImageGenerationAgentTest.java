package cn.nuist.aiarticlewriter.agent.agents;

import cn.nuist.aiarticlewriter.model.enums.ImageMediaTypeEnum;
import cn.nuist.aiarticlewriter.model.enums.ImageMethodEnum;
import cn.nuist.aiarticlewriter.model.image.ImageAsset;
import cn.nuist.aiarticlewriter.model.image.ImageRequest;
import cn.nuist.aiarticlewriter.model.state.article.ImageRequirement;
import cn.nuist.aiarticlewriter.model.state.article.ImageResult;
import cn.nuist.aiarticlewriter.service.ImageService;
import cn.nuist.aiarticlewriter.service.ImageStorageService;
import cn.nuist.aiarticlewriter.service.ImageStrategySelector;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ImageGenerationAgentTest {

    @Test
    void shouldUsePexelsUrlDirectlyWithoutUploadingToStorage() {
        String pexelsUrl = "https://images.pexels.com/photos/1/pexels-photo-1.jpeg";
        ImageRequirement requirement = ImageRequirement.builder()
                .position(2)
                .type("section")
                .sectionTitle("Planning")
                .keywords("workspace planning")
                .imageSource(ImageMethodEnum.PEXELS.getValue())
                .placeholderId("{{IMAGE_PLACEHOLDER_2}}")
                .build();
        ImageService pexelsService = mock(ImageService.class);
        when(pexelsService.getMethod()).thenReturn(ImageMethodEnum.PEXELS);
        when(pexelsService.acquire(any(ImageRequest.class))).thenReturn(ImageAsset.builder()
                .method(ImageMethodEnum.PEXELS)
                .mediaType(ImageMediaTypeEnum.IMAGE_URL)
                .url(pexelsUrl)
                .contentType("image/jpeg")
                .description("section")
                .build());

        ImageStrategySelector selector = mock(ImageStrategySelector.class);
        when(selector.select(any(ImageRequest.class))).thenReturn(pexelsService);
        ImageStorageService storageService = mock(ImageStorageService.class);
        ImageGenerationAgent agent = new ImageGenerationAgent(selector, List.of(pexelsService), storageService,
                new ObjectMapper());

        List<ImageResult> results = agent.generateImages(List.of(requirement));

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getUrl()).isEqualTo(pexelsUrl);
        assertThat(results.getFirst().getMethod()).isEqualTo(ImageMethodEnum.PEXELS.getValue());
        assertThat(results.getFirst().getPlaceholderId()).isEqualTo("{{IMAGE_PLACEHOLDER_2}}");
        verify(storageService, never()).upload(any(ImageAsset.class), anyString());
    }
}
