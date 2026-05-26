package cn.nuist.aiarticlewriter.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.nuist.aiarticlewriter.config.CosProperties;
import cn.nuist.aiarticlewriter.model.enums.ImageMediaTypeEnum;
import cn.nuist.aiarticlewriter.model.image.ImageAsset;
import cn.nuist.aiarticlewriter.service.ImageStorageService;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.region.Region;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Tencent Cloud COS image storage implementation.
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "image-storage.cos", name = "enabled", havingValue = "true")
public class CosImageStorageServiceImpl implements ImageStorageService {

    private final CosProperties cosProperties;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String upload(ImageAsset asset, String objectKey) {
        if (asset == null) {
            return null;
        }
        if (asset.getMediaType() == ImageMediaTypeEnum.IMAGE_URL) {
            return uploadFromUrl(asset.getUrl(), objectKey);
        }
        if (asset.getBytes() != null && asset.getBytes().length > 0) {
            return uploadBytes(asset.getBytes(), objectKey, asset.getContentType());
        }
        if (asset.getMediaType() == ImageMediaTypeEnum.BASE64_IMAGE) {
            return uploadBytes(decodeBase64(asset.getContent()), objectKey, asset.getContentType());
        }
        if (asset.getContent() != null) {
            return uploadText(asset.getContent(), objectKey, asset.getContentType());
        }
        return null;
    }

    @Override
    public String uploadFromUrl(String imageUrl, String objectKey) {
        byte[] bytes = restTemplate.getForObject(imageUrl, byte[].class);
        return uploadBytes(bytes, objectKey, ImageMediaTypeEnum.IMAGE_URL.getContentType());
    }

    @Override
    public String uploadBytes(byte[] bytes, String objectKey, String contentType) {
        validateConfig();
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        COSClient cosClient = buildCosClient();
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(bytes.length);
            if (StrUtil.isNotBlank(contentType)) {
                metadata.setContentType(contentType);
            }
            cosClient.putObject(cosProperties.getBucketName(), objectKey, inputStream, metadata);
            return buildPublicUrl(objectKey);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to upload image to COS", e);
        } finally {
            cosClient.shutdown();
        }
    }

    @Override
    public String uploadText(String content, String objectKey, String contentType) {
        if (content == null) {
            return null;
        }
        return uploadBytes(content.getBytes(StandardCharsets.UTF_8), objectKey, contentType);
    }

    private COSClient buildCosClient() {
        COSCredentials credentials = new BasicCOSCredentials(cosProperties.getSecretId(), cosProperties.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(cosProperties.getRegion()));
        clientConfig.setHttpProtocol(HttpProtocol.https);
        return new COSClient(credentials, clientConfig);
    }

    private void validateConfig() {
        if (StrUtil.hasBlank(cosProperties.getSecretId(), cosProperties.getSecretKey(),
                cosProperties.getRegion(), cosProperties.getBucketName())) {
            throw new IllegalStateException("COS storage is enabled but configuration is incomplete");
        }
    }

    private String buildPublicUrl(String objectKey) {
        if (StrUtil.isNotBlank(cosProperties.getDomain())) {
            return cosProperties.getDomain().replaceAll("/+$", "") + "/" + objectKey;
        }
        return "https://" + cosProperties.getBucketName() + ".cos." + cosProperties.getRegion()
                + ".myqcloud.com/" + objectKey;
    }

    private byte[] decodeBase64(String content) {
        if (StrUtil.isBlank(content)) {
            return new byte[0];
        }
        String payload = content;
        int commaIndex = content.indexOf(',');
        if (commaIndex >= 0) {
            payload = content.substring(commaIndex + 1);
        }
        return Base64.getDecoder().decode(payload);
    }
}
