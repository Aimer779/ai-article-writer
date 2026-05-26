package cn.nuist.aiarticlewriter.service;

import cn.nuist.aiarticlewriter.model.enums.ImageMethodEnum;
import cn.nuist.aiarticlewriter.model.image.ImageAsset;
import cn.nuist.aiarticlewriter.model.image.ImageRequest;

/**
 * Image acquisition service interface.
 */
public interface ImageService {

    /**
     * Get image acquisition method.
     *
     * @return image method enum
     */
    ImageMethodEnum getMethod();

    /**
     * Whether this service can handle the request.
     *
     * @param request image request
     * @return true if supported
     */
    boolean supports(ImageRequest request);

    /**
     * Acquire image asset.
     *
     * @param request image request
     * @return image asset
     */
    ImageAsset acquire(ImageRequest request);
}
