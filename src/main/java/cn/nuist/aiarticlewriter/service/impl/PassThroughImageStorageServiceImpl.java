package cn.nuist.aiarticlewriter.service.impl;

import cn.nuist.aiarticlewriter.model.enums.ImageMediaTypeEnum;
import cn.nuist.aiarticlewriter.model.image.ImageAsset;
import cn.nuist.aiarticlewriter.service.ImageStorageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Pass-through image storage implementation used when COS storage is disabled.
 */
@Service
@ConditionalOnProperty(prefix = "image-storage.cos", name = "enabled", havingValue = "false", matchIfMissing = true)
public class PassThroughImageStorageServiceImpl implements ImageStorageService {

    @Override
    public String upload(ImageAsset asset, String objectKey) {
        if (asset == null) {
            return null;
        }
        if (asset.getMediaType() == ImageMediaTypeEnum.IMAGE_URL) {
            return uploadFromUrl(asset.getUrl(), objectKey);
        }
        if (asset.getContent() != null) {
            return uploadText(asset.getContent(), objectKey, asset.getContentType());
        }
        return null;
    }

    @Override
    public String uploadFromUrl(String imageUrl, String objectKey) {
        return imageUrl;
    }

    @Override
    public String uploadBytes(byte[] bytes, String objectKey, String contentType) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return "data:" + contentType + ";base64," + java.util.Base64.getEncoder().encodeToString(bytes);
    }

    @Override
    public String uploadText(String content, String objectKey, String contentType) {
        if (content == null) {
            return null;
        }
        return uploadBytes(content.getBytes(StandardCharsets.UTF_8), objectKey, contentType);
    }
}
