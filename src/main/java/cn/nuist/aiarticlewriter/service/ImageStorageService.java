package cn.nuist.aiarticlewriter.service;

import cn.nuist.aiarticlewriter.model.image.ImageAsset;

/**
 * Image storage service interface.
 */
public interface ImageStorageService {

    /**
     * Upload image asset to object storage.
     *
     * @param asset image asset
     * @param objectKey target object key
     * @return stored image URL
     */
    String upload(ImageAsset asset, String objectKey);

    /**
     * Upload image from URL to object storage.
     *
     * @param imageUrl source image URL
     * @param objectKey target object key
     * @return stored image URL
     */
    String uploadFromUrl(String imageUrl, String objectKey);

    /**
     * Upload bytes to object storage.
     *
     * @param bytes file bytes
     * @param objectKey target object key
     * @param contentType MIME content type
     * @return stored image URL
     */
    String uploadBytes(byte[] bytes, String objectKey, String contentType);

    /**
     * Upload text content to object storage.
     *
     * @param content file content
     * @param objectKey target object key
     * @param contentType MIME content type
     * @return stored image URL
     */
    String uploadText(String content, String objectKey, String contentType);
}
