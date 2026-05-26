package cn.nuist.aiarticlewriter.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.nuist.aiarticlewriter.config.AiImageProperties;
import cn.nuist.aiarticlewriter.model.enums.ImageMediaTypeEnum;
import cn.nuist.aiarticlewriter.model.enums.ImageMethodEnum;
import cn.nuist.aiarticlewriter.model.image.ImageAsset;
import cn.nuist.aiarticlewriter.model.image.ImageRequest;
import cn.nuist.aiarticlewriter.service.ImageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DashScope Qwen image generation service implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiImageGenerationServiceImpl implements ImageService {

    private static final String PNG_CONTENT_TYPE = "image/png";

    private final AiImageProperties aiImageProperties;

    private final ObjectMapper objectMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Configure HTTP timeout after properties are bound.
     */
    @PostConstruct
    void configureRestTemplate() {
        int timeout = resolveTimeout();
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);
        restTemplate.setRequestFactory(requestFactory);
    }

    @Override
    public ImageMethodEnum getMethod() {
        return ImageMethodEnum.AI_GENERATION;
    }

    @Override
    public boolean supports(ImageRequest request) {
        return request != null
                && StrUtil.isNotBlank(aiImageProperties.getApiKey())
                && StrUtil.isNotBlank(getPrompt(request));
    }

    @Override
    public ImageAsset acquire(ImageRequest request) {
        String prompt = getPrompt(request);
        if (StrUtil.isBlank(prompt)) {
            return null;
        }
        try {
            String imageUrl = generateImageUrl(prompt);
            if (StrUtil.isBlank(imageUrl)) {
                log.warn("DashScope image generation returned blank image URL, position={}", request.getPosition());
                return null;
            }
            log.info("AI image generated successfully, position={}", request.getPosition());
            return ImageAsset.builder()
                    .method(getMethod())
                    .mediaType(ImageMediaTypeEnum.IMAGE_URL)
                    .url(imageUrl)
                    .contentType(PNG_CONTENT_TYPE)
                    .fileName("ai-generated-" + request.getPosition() + ".png")
                    .storageFolder(aiImageProperties.getFolder())
                    .altText(request.getSectionTitle())
                    .description(prompt)
                    .build();
        } catch (Exception e) {
            log.warn("Failed to generate AI image, position={}, sectionTitle={}",
                    request.getPosition(), request.getSectionTitle(), e);
            return null;
        }
    }

    private String generateImageUrl(String prompt) {
        if (StrUtil.hasBlank(aiImageProperties.getApiKey(), aiImageProperties.getBaseUrl(),
                aiImageProperties.getModel())) {
            log.debug("DashScope image generation configuration is incomplete");
            return null;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(aiImageProperties.getApiKey());
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(buildRequestBody(prompt), headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(aiImageProperties.getBaseUrl(), entity,
                    String.class);
            return parseImageUrl(response.getBody());
        } catch (RestClientException e) {
            log.warn("Failed to call DashScope image generation API", e);
            return null;
        }
    }

    private Map<String, Object> buildRequestBody(String prompt) {
        Map<String, Object> textContent = Map.of("text", prompt);
        Map<String, Object> message = Map.of(
                "role", "user",
                "content", List.of(textContent)
        );
        Map<String, Object> input = Map.of("messages", List.of(message));
        Map<String, Object> parameters = new LinkedHashMap<>();
        if (StrUtil.isNotBlank(aiImageProperties.getNegativePrompt())) {
            parameters.put("negative_prompt", aiImageProperties.getNegativePrompt());
        }
        if (StrUtil.isNotBlank(aiImageProperties.getSize())) {
            parameters.put("size", aiImageProperties.getSize());
        }
        parameters.put("prompt_extend", Boolean.TRUE.equals(aiImageProperties.getPromptExtend()));
        parameters.put("watermark", Boolean.TRUE.equals(aiImageProperties.getWatermark()));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", aiImageProperties.getModel());
        body.put("input", input);
        body.put("parameters", parameters);
        return body;
    }

    private String parseImageUrl(String responseBody) {
        if (StrUtil.isBlank(responseBody)) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            String errorCode = root.path("code").asText(null);
            if (StrUtil.isNotBlank(errorCode)) {
                log.warn("DashScope image generation failed, code={}, message={}",
                        errorCode, root.path("message").asText(null));
                return null;
            }
            String multimodalImageUrl = parseMultimodalImageUrl(root);
            if (StrUtil.isNotBlank(multimodalImageUrl)) {
                return multimodalImageUrl;
            }
            return parseImageSynthesisUrl(root);
        } catch (Exception e) {
            log.warn("Failed to parse DashScope image generation response", e);
            return null;
        }
    }

    private String parseMultimodalImageUrl(JsonNode root) {
        JsonNode choices = root.path("output").path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            return null;
        }
        JsonNode content = choices.get(0).path("message").path("content");
        if (!content.isArray()) {
            return null;
        }
        for (JsonNode item : content) {
            String imageUrl = item.path("image").asText(null);
            if (StrUtil.isNotBlank(imageUrl)) {
                return imageUrl;
            }
        }
        return null;
    }

    private String parseImageSynthesisUrl(JsonNode root) {
        JsonNode results = root.path("output").path("results");
        if (!results.isArray() || results.isEmpty()) {
            return null;
        }
        String imageUrl = results.get(0).path("url").asText(null);
        return StrUtil.isBlank(imageUrl) ? null : imageUrl;
    }

    private String getPrompt(ImageRequest request) {
        if (request == null) {
            return null;
        }
        if (StrUtil.isNotBlank(request.getPrompt())) {
            return request.getPrompt();
        }
        return request.getKeywords();
    }

    private int resolveTimeout() {
        Integer timeout = aiImageProperties.getTimeout();
        return timeout == null || timeout <= 0 ? 60000 : timeout;
    }
}
