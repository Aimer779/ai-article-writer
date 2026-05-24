package cn.nuist.aiarticlewriter.service;

/**
 * Image storage service interface.
 */
public interface ImageStorageService {

    /**
     * Upload an image from URL to object storage.
     *
     * @param imageUrl source image URL
     * @param objectKey target object key
     * @return stored image URL
     */
    String uploadImageFromUrl(String imageUrl, String objectKey);
}
