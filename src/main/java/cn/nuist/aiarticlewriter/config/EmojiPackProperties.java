package cn.nuist.aiarticlewriter.config;

import cn.nuist.aiarticlewriter.constant.ArticleConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Meme image search configuration properties.
 */
@Data
@Component
@ConfigurationProperties(prefix = "emoji-pack")
public class EmojiPackProperties {

    /**
     * Bing image search URL.
     */
    private String searchUrl = ArticleConstant.BING_IMAGE_SEARCH_URL;

    /**
     * Meme keyword suffix appended by the service.
     */
    private String suffix = ArticleConstant.EMOJI_PACK_SUFFIX;

    /**
     * Request timeout in milliseconds.
     */
    private Integer timeout = 10000;

    /**
     * Blocked source domains for low-quality or irrelevant images.
     */
    private List<String> blockedDomains = new ArrayList<>(List.of(
            "tumblr.com",
            "pinterest.com",
            "pinimg.com",
            "facebook.com",
            "instagram.com",
            "twitter.com",
            "x.com"
    ));
}
