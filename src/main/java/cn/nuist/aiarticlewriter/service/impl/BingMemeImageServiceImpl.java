package cn.nuist.aiarticlewriter.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.nuist.aiarticlewriter.config.EmojiPackProperties;
import cn.nuist.aiarticlewriter.constant.ArticleConstant;
import cn.nuist.aiarticlewriter.model.enums.ImageMediaTypeEnum;
import cn.nuist.aiarticlewriter.model.enums.ImageMethodEnum;
import cn.nuist.aiarticlewriter.model.image.ImageAsset;
import cn.nuist.aiarticlewriter.model.image.ImageRequest;
import cn.nuist.aiarticlewriter.service.ImageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Bing meme image search service implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BingMemeImageServiceImpl implements ImageService {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Safari/537.36";

    private final EmojiPackProperties emojiPackProperties;

    private final ObjectMapper objectMapper;

    @Override
    public ImageMethodEnum getMethod() {
        return ImageMethodEnum.MEME;
    }

    @Override
    public boolean supports(ImageRequest request) {
        return request != null && StrUtil.isNotBlank(getSearchText(request));
    }

    @Override
    public ImageAsset acquire(ImageRequest request) {
        String query = buildQuery(getSearchText(request));
        try {
            String imageUrl = searchFirstImageUrl(query);
            if (StrUtil.isBlank(imageUrl)) {
                return null;
            }
            return ImageAsset.builder()
                    .method(getMethod())
                    .mediaType(ImageMediaTypeEnum.IMAGE_URL)
                    .url(imageUrl)
                    .contentType(ImageMediaTypeEnum.IMAGE_URL.getContentType())
                    .fileName("meme-" + request.getPosition() + ".jpg")
                    .altText(request.getSectionTitle())
                    .description(request.getType())
                    .build();
        } catch (Exception e) {
            log.warn("Failed to search meme image from Bing, query={}", query, e);
            return null;
        }
    }

    private String searchFirstImageUrl(String query) throws IOException {
        Document document = Jsoup.connect(emojiPackProperties.getSearchUrl())
                .userAgent(USER_AGENT)
                .timeout(resolveTimeout())
                .ignoreContentType(true)
                .data("q", query)
                .data("first", "1")
                .data("count", String.valueOf(ArticleConstant.BING_MAX_IMAGES))
                .data("mmasync", "1")
                .get();
        Set<String> imageUrls = extractImageUrls(document);
        return imageUrls.stream().findFirst().orElse(null);
    }

    private Set<String> extractImageUrls(Document document) {
        Set<String> imageUrls = new LinkedHashSet<>();
        for (Element element : document.select("a.iusc[m]")) {
            BingImageCandidate candidate = parseImageCandidateFromMetadata(element.attr("m"));
            if (candidate != null && hasMemeSignal(candidate)) {
                addValidImageUrl(imageUrls, candidate.mediaUrl());
                addValidImageUrl(imageUrls, candidate.thumbnailUrl());
            }
        }
        for (Element element : document.select("img.mimg, img[src], img[data-src]")) {
            String imageUrl = firstNonBlank(element.attr("data-src"), element.attr("src"));
            addValidImageUrl(imageUrls, imageUrl);
        }
        return imageUrls;
    }

    private BingImageCandidate parseImageCandidateFromMetadata(String metadata) {
        if (StrUtil.isBlank(metadata)) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(metadata);
            return new BingImageCandidate(
                    root.path("murl").asText(null),
                    root.path("turl").asText(null),
                    root.path("purl").asText(null),
                    root.path("t").asText(null)
            );
        } catch (Exception e) {
            log.debug("Failed to parse Bing image metadata", e);
            return null;
        }
    }

    private void addValidImageUrl(Set<String> imageUrls, String imageUrl) {
        if (isHttpImageUrl(imageUrl)) {
            imageUrls.add(imageUrl);
        }
    }

    private boolean hasMemeSignal(BingImageCandidate candidate) {
        String text = String.join(" ",
                nullToEmpty(candidate.mediaUrl()),
                nullToEmpty(candidate.thumbnailUrl()),
                nullToEmpty(candidate.pageUrl()),
                nullToEmpty(candidate.title())
        ).toLowerCase(Locale.ROOT);
        return text.contains("表情包")
                || text.contains("表情")
                || text.contains("meme")
                || text.contains("搞笑")
                || text.contains("斗图");
    }

    private String buildQuery(String text) {
        String query = text.trim();
        String suffix = emojiPackProperties.getSuffix();
        if (StrUtil.isNotBlank(suffix) && !query.contains(suffix)) {
            query = query + " " + suffix.trim();
        }
        return query;
    }

    private String getSearchText(ImageRequest request) {
        if (StrUtil.isNotBlank(request.getKeywords())) {
            return request.getKeywords();
        }
        return request.getPrompt();
    }

    private boolean isHttpImageUrl(String imageUrl) {
        return StrUtil.isNotBlank(imageUrl)
                && (imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))
                && !imageUrl.contains("r.bing.com")
                && !imageUrl.contains("th.bing.com/th/id/OIP")
                && !isBlockedDomain(imageUrl);
    }

    private boolean isBlockedDomain(String imageUrl) {
        String host = parseHost(imageUrl);
        if (StrUtil.isBlank(host)) {
            return true;
        }
        List<String> blockedDomains = emojiPackProperties.getBlockedDomains();
        if (blockedDomains == null || blockedDomains.isEmpty()) {
            return false;
        }
        return blockedDomains.stream()
                .filter(StrUtil::isNotBlank)
                .map(domain -> domain.trim().toLowerCase(Locale.ROOT))
                .anyMatch(domain -> host.equals(domain) || host.endsWith("." + domain));
    }

    private String parseHost(String imageUrl) {
        try {
            String host = new URI(imageUrl).getHost();
            if (host == null) {
                return null;
            }
            return host.toLowerCase(Locale.ROOT);
        } catch (URISyntaxException e) {
            log.debug("Invalid meme image URL: {}", imageUrl, e);
            return null;
        }
    }

    private String firstNonBlank(String first, String second) {
        return StrUtil.isNotBlank(first) ? first : second;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private int resolveTimeout() {
        Integer timeout = emojiPackProperties.getTimeout();
        return timeout == null || timeout <= 0 ? 10000 : timeout;
    }

    private record BingImageCandidate(String mediaUrl, String thumbnailUrl, String pageUrl, String title) {
    }
}
