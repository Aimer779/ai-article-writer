package cn.nuist.aiarticlewriter.service;

import cn.hutool.core.util.StrUtil;
import cn.nuist.aiarticlewriter.model.enums.ImageMethodEnum;
import cn.nuist.aiarticlewriter.model.image.ImageRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Selects the best image service for an image request.
 */
@Component
@RequiredArgsConstructor
public class ImageStrategySelector {

    private final List<ImageService> imageServices;

    /**
     * Select an image service for the request.
     *
     * @param request image request
     * @return selected image service
     */
    public ImageService select(ImageRequest request) {
        ImageService preferredService = findService(request.getPreferredMethod(), request);
        if (preferredService != null) {
            return preferredService;
        }
        ImageMethodEnum routedMethod = routeByVisualType(request);
        ImageService routedService = findService(routedMethod, request);
        if (routedService != null) {
            return routedService;
        }
        ImageService pexelsService = findService(ImageMethodEnum.PEXELS, request);
        if (pexelsService != null) {
            return pexelsService;
        }
        return findService(ImageMethodEnum.PICSUM, request);
    }

    private ImageService findService(ImageMethodEnum method, ImageRequest request) {
        if (method == null) {
            return null;
        }
        return imageServices.stream()
                .filter(service -> method.equals(service.getMethod()))
                .filter(service -> service.supports(request))
                .findFirst()
                .orElse(null);
    }

    private ImageMethodEnum routeByVisualType(ImageRequest request) {
        String visualType = request.getVisualType();
        if (StrUtil.isBlank(visualType)) {
            return ImageMethodEnum.PEXELS;
        }
        String normalizedType = visualType.trim().toLowerCase();
        if (normalizedType.contains("flow") || normalizedType.contains("process")
                || normalizedType.contains("sequence") || normalizedType.contains("mermaid")) {
            return ImageMethodEnum.MERMAID;
        }
        if (normalizedType.contains("svg") || normalizedType.contains("diagram")
                || normalizedType.contains("architecture") || normalizedType.contains("model")) {
            return ImageMethodEnum.SVG_DIAGRAM;
        }
        if (normalizedType.contains("meme") || normalizedType.contains("humor")
                || normalizedType.contains("reaction")) {
            return ImageMethodEnum.MEME;
        }
        if (normalizedType.contains("ai") || normalizedType.contains("generated")
                || normalizedType.contains("abstract") || normalizedType.contains("concept")) {
            return ImageMethodEnum.AI_GENERATION;
        }
        return ImageMethodEnum.PEXELS;
    }
}
