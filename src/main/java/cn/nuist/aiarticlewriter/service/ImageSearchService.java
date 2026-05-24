package cn.nuist.aiarticlewriter.service;

import cn.nuist.aiarticlewriter.model.enums.ImageMethodEnum;

/**
 * Image search service interface.
 *
 * Abstracts image search logic so multiple image sources can be added later,
 * such as Pexels, Unsplash, or AI image generation.
 */
public interface ImageSearchService {

    /**
     * Search image by keywords.
     *
     * @param keywords search keywords
     * @return image URL, or null when search fails
     */
    String searchImage(String keywords);

    /**
     * Get image acquisition method.
     *
     * @return image method enum
     */
    ImageMethodEnum getMethod();

    /**
     * Get fallback image URL.
     *
     * @param position position number used to generate a stable random image
     * @return fallback image URL
     */
    String getFallbackImage(int position);
}
