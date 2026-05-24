package cn.nuist.aiarticlewriter.agent.agents;

import cn.nuist.aiarticlewriter.model.enums.ImageMethodEnum;
import cn.nuist.aiarticlewriter.model.state.article.ImageRequirement;
import cn.nuist.aiarticlewriter.model.state.article.ImageResult;
import cn.nuist.aiarticlewriter.service.ImageSearchService;
import cn.nuist.aiarticlewriter.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Agent responsible for acquiring article images and storing them.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ImageGenerationAgent {

    private final List<ImageSearchService> imageSearchServices;

    private final ImageStorageService imageStorageService;

    /**
     * Generate image results from image requirements.
     *
     * @param requirements image requirements
     * @return generated image results
     */
    public List<ImageResult> generateImages(List<ImageRequirement> requirements) {
        List<ImageResult> imageResults = new ArrayList<>();
        if (requirements == null || requirements.isEmpty()) {
            return imageResults;
        }
        ImageSearchService searchService = getPrimarySearchService();
        List<ImageRequirement> sortedRequirements = requirements.stream()
                .sorted(Comparator.comparing(ImageRequirement::getPosition))
                .toList();
        for (ImageRequirement requirement : sortedRequirements) {
            ImageMethodEnum method = ImageMethodEnum.PEXELS;
            String imageUrl = searchService.searchImage(requirement.getKeywords());
            if (imageUrl == null || imageUrl.isBlank()) {
                imageUrl = searchService.getFallbackImage(requirement.getPosition());
                method = ImageMethodEnum.PICSUM;
            }
            String storedUrl = imageStorageService.uploadImageFromUrl(imageUrl, buildObjectKey(requirement, method));
            imageResults.add(buildImageResult(requirement, storedUrl, method));
            log.info("ImageGenerationAgent generated image, position={}, method={}, sectionTitle={}",
                    requirement.getPosition(), method.getValue(), requirement.getSectionTitle());
        }
        log.info("ImageGenerationAgent completed image generation, count={}", imageResults.size());
        return imageResults;
    }

    private ImageSearchService getPrimarySearchService() {
        return imageSearchServices.stream()
                .filter(imageSearchService -> ImageMethodEnum.PEXELS.equals(imageSearchService.getMethod()))
                .findFirst()
                .orElse(imageSearchServices.getFirst());
    }

    private ImageResult buildImageResult(ImageRequirement requirement, String imageUrl, ImageMethodEnum method) {
        return ImageResult.builder()
                .position(requirement.getPosition())
                .url(imageUrl)
                .method(method.getValue())
                .keywords(requirement.getKeywords())
                .sectionTitle(requirement.getSectionTitle())
                .description(requirement.getType())
                .build();
    }

    private String buildObjectKey(ImageRequirement requirement, ImageMethodEnum method) {
        return "article-images/" + method.getValue().toLowerCase() + "/" + requirement.getPosition() + ".jpg";
    }
}
