package cn.nuist.aiarticlewriter.agent.agents;

import cn.nuist.aiarticlewriter.model.enums.ImageMethodEnum;
import cn.nuist.aiarticlewriter.model.image.ImageAsset;
import cn.nuist.aiarticlewriter.model.image.ImageRequest;
import cn.nuist.aiarticlewriter.model.state.article.ImageRequirement;
import cn.nuist.aiarticlewriter.model.state.article.ImageResult;
import cn.nuist.aiarticlewriter.service.ImageService;
import cn.nuist.aiarticlewriter.service.ImageStorageService;
import cn.nuist.aiarticlewriter.service.ImageStrategySelector;
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

    private final ImageStrategySelector imageStrategySelector;

    private final List<ImageService> imageServices;

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
        List<ImageRequirement> sortedRequirements = requirements.stream()
                .sorted(Comparator.comparing(ImageRequirement::getPosition))
                .toList();
        for (ImageRequirement requirement : sortedRequirements) {
            imageResults.add(generateImage(requirement));
        }
        log.info("ImageGenerationAgent completed image generation, count={}", imageResults.size());
        return imageResults;
    }

    private ImageResult generateImage(ImageRequirement requirement) {
        ImageRequest request = buildImageRequest(requirement);
        try {
            ImageService imageService = imageStrategySelector.select(request);
            if (imageService == null) {
                throw new IllegalStateException("No image service available");
            }
            ImageAsset asset = imageService.acquire(request);
            validateAsset(asset);
            String storedUrl = imageStorageService.upload(asset, buildObjectKey(requirement, asset.getMethod(), asset));
            if (storedUrl == null || storedUrl.isBlank()) {
                throw new IllegalStateException("Image storage returned blank URL");
            }
            ImageResult imageResult = buildImageResult(requirement, storedUrl, asset);
            log.info("ImageGenerationAgent generated image, position={}, method={}, sectionTitle={}",
                    requirement.getPosition(), asset.getMethod().getValue(), requirement.getSectionTitle());
            return imageResult;
        } catch (Exception e) {
            log.warn("Image generation failed, fallback to Picsum, position={}, sectionTitle={}",
                    requirement.getPosition(), requirement.getSectionTitle(), e);
            return fallbackToPicsum(requirement, request);
        }
    }

    private ImageResult fallbackToPicsum(ImageRequirement requirement, ImageRequest request) {
        ImageRequest fallbackRequest = ImageRequest.builder()
                .position(request.getPosition())
                .type(request.getType())
                .sectionTitle(request.getSectionTitle())
                .keywords(request.getKeywords())
                .prompt(request.getPrompt())
                .visualType(request.getVisualType())
                .aspectRatio(request.getAspectRatio())
                .style(request.getStyle())
                .preferredMethod(ImageMethodEnum.PICSUM)
                .build();
        ImageAsset fallbackAsset = getPicsumImageService().acquire(fallbackRequest);
        return buildImageResult(requirement, fallbackAsset.getUrl(), fallbackAsset);
    }

    private ImageService getPicsumImageService() {
        return imageServices.stream()
                .filter(imageService -> ImageMethodEnum.PICSUM.equals(imageService.getMethod()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Picsum image service is not available"));
    }

    private ImageRequest buildImageRequest(ImageRequirement requirement) {
        return ImageRequest.builder()
                .position(requirement.getPosition())
                .type(requirement.getType())
                .sectionTitle(requirement.getSectionTitle())
                .keywords(requirement.getKeywords())
                .prompt(requirement.getPrompt() == null ? requirement.getKeywords() : requirement.getPrompt())
                .visualType(requirement.getVisualType())
                .aspectRatio(requirement.getAspectRatio())
                .style(requirement.getStyle())
                .preferredMethod(ImageMethodEnum.getEnumByValue(requirement.getPreferredMethod()))
                .build();
    }

    private void validateAsset(ImageAsset asset) {
        if (asset == null) {
            throw new IllegalStateException("Image asset cannot be null");
        }
        if (asset.getMethod() == null) {
            throw new IllegalStateException("Image asset method cannot be null");
        }
        if (asset.getMediaType() == null) {
            throw new IllegalStateException("Image asset media type cannot be null");
        }
        if ((asset.getUrl() == null || asset.getUrl().isBlank())
                && (asset.getContent() == null || asset.getContent().isBlank())
                && (asset.getBytes() == null || asset.getBytes().length == 0)) {
            throw new IllegalStateException("Image asset must contain URL, content, or bytes");
        }
    }

    private ImageResult buildImageResult(ImageRequirement requirement, String imageUrl, ImageAsset asset) {
        return ImageResult.builder()
                .position(requirement.getPosition())
                .url(imageUrl)
                .method(asset.getMethod().getValue())
                .mediaType(asset.getMediaType().getValue())
                .keywords(requirement.getKeywords())
                .sectionTitle(requirement.getSectionTitle())
                .description(asset.getDescription() == null ? requirement.getType() : asset.getDescription())
                .build();
    }

    private String buildObjectKey(ImageRequirement requirement, ImageMethodEnum method, ImageAsset asset) {
        String extension = resolveExtension(asset);
        String folder = asset.getStorageFolder();
        if (folder == null || folder.isBlank()) {
            folder = method.getValue().toLowerCase();
        }
        folder = folder.replace('\\', '/').replaceAll("^/+", "").replaceAll("/+$", "");
        return "article-images/" + folder + "/" + requirement.getPosition() + extension;
    }

    private String resolveExtension(ImageAsset asset) {
        if (asset.getFileName() != null && asset.getFileName().contains(".")) {
            return asset.getFileName().substring(asset.getFileName().lastIndexOf('.'));
        }
        if (asset.getContentType() != null && asset.getContentType().contains("svg")) {
            return ".svg";
        }
        if (asset.getContentType() != null && asset.getContentType().contains("png")) {
            return ".png";
        }
        if (asset.getContentType() != null && asset.getContentType().contains("pdf")) {
            return ".pdf";
        }
        return ".jpg";
    }
}
