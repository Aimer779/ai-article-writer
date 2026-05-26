package cn.nuist.aiarticlewriter.service.impl;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;

/**
 * Pexels image search service implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PexelsImageSearchServiceImpl implements ImageService {

    private final ObjectMapper objectMapper;

    @Value("${image-search.pexels.api-key:}")
    private String pexelsApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public ImageMethodEnum getMethod() {
        return ImageMethodEnum.PEXELS;
    }

    @Override
    public boolean supports(ImageRequest request) {
        return request != null && request.getKeywords() != null && !request.getKeywords().isBlank();
    }

    @Override
    public ImageAsset acquire(ImageRequest request) {
        String imageUrl = searchImage(request.getKeywords());
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;
        }
        return ImageAsset.builder()
                .method(getMethod())
                .mediaType(ImageMediaTypeEnum.IMAGE_URL)
                .url(imageUrl)
                .contentType(ImageMediaTypeEnum.IMAGE_URL.getContentType())
                .fileName("pexels-" + request.getPosition() + ".jpg")
                .altText(request.getSectionTitle())
                .description(request.getType())
                .build();
    }

    private String searchImage(String keywords) {
        if (keywords == null || keywords.isBlank()) {
            return null;
        }
        if (pexelsApiKey == null || pexelsApiKey.isBlank()) {
            log.debug("Pexels API key is not configured");
            return null;
        }

        String requestUrl = UriComponentsBuilder.fromUriString(ArticleConstant.PEXELS_API_URL)
                .queryParam("query", keywords.trim())
                .queryParam("per_page", ArticleConstant.PEXELS_PER_PAGE)
                .queryParam("orientation", ArticleConstant.PEXELS_ORIENTATION_LANDSCAPE)
                .encode(StandardCharsets.UTF_8)
                .toUriString();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", pexelsApiKey);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(requestUrl, HttpMethod.GET, requestEntity,
                    String.class);
            return parseFirstImageUrl(response.getBody());
        } catch (RestClientException e) {
            log.warn("Failed to search image from Pexels, keywords={}", keywords, e);
            return null;
        }
    }

    private String parseFirstImageUrl(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode photos = root.path("photos");
            if (!photos.isArray() || photos.isEmpty()) {
                return null;
            }
            JsonNode src = photos.get(0).path("src");
            String large2x = src.path("large2x").asText(null);
            if (large2x != null && !large2x.isBlank()) {
                return large2x;
            }
            String large = src.path("large").asText(null);
            if (large != null && !large.isBlank()) {
                return large;
            }
            String original = src.path("original").asText(null);
            return original == null || original.isBlank() ? null : original;
        } catch (Exception e) {
            log.warn("Failed to parse Pexels image search response", e);
            return null;
        }
    }
}
