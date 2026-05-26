package cn.nuist.aiarticlewriter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Tencent Cloud COS configuration properties.
 */
@Data
@Component
@ConfigurationProperties(prefix = "image-storage.cos")
public class CosProperties {

    /**
     * Whether COS storage is enabled.
     */
    private boolean enabled;

    /**
     * Tencent Cloud secret ID.
     */
    private String secretId;

    /**
     * Tencent Cloud secret key.
     */
    private String secretKey;

    /**
     * COS region, such as ap-shanghai.
     */
    private String region;

    /**
     * COS bucket name with appId suffix.
     */
    private String bucketName;

    /**
     * Optional custom public domain.
     */
    private String domain;
}
