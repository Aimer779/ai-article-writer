package cn.nuist.aiarticlewriter.service.impl;

import cn.nuist.aiarticlewriter.config.AiImageProperties;
import cn.nuist.aiarticlewriter.model.enums.ImageMediaTypeEnum;
import cn.nuist.aiarticlewriter.model.enums.ImageMethodEnum;
import cn.nuist.aiarticlewriter.model.image.ImageAsset;
import cn.nuist.aiarticlewriter.model.image.ImageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class AiImageGenerationServiceImplTest {

    private AiImageGenerationServiceImpl aiImageGenerationService;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        AiImageProperties properties = new AiImageProperties();
        properties.setApiKey("test-api-key");
        properties.setBaseUrl("https://dashscope.example.com/api/v1/services/aigc/multimodal-generation/generation");
        properties.setModel("qwen-image-2.0");
        properties.setSize("1664*928");
        properties.setFolder("ai-generated");
        properties.setNegativePrompt("low quality");

        aiImageGenerationService = new AiImageGenerationServiceImpl(properties, new ObjectMapper());
        aiImageGenerationService.configureRestTemplate();
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(aiImageGenerationService,
                "restTemplate");
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @Test
    void shouldGenerateAiImageAssetFromDashScopeResponse() {
        mockServer.expect(requestTo("https://dashscope.example.com/api/v1/services/aigc/multimodal-generation/generation"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-api-key"))
                .andExpect(jsonPath("$.model").value("qwen-image-2.0"))
                .andExpect(jsonPath("$.input.messages[0].content[0].text").value("Create a modern cover image"))
                .andExpect(jsonPath("$.parameters.size").value("1664*928"))
                .andExpect(jsonPath("$.parameters.prompt_extend").value(true))
                .andExpect(jsonPath("$.parameters.watermark").value(false))
                .andRespond(withSuccess("""
                        {
                          "output": {
                            "choices": [
                              {
                                "finish_reason": "stop",
                                "message": {
                                  "role": "assistant",
                                  "content": [
                                    {
                                      "image": "https://dashscope-result.example.com/generated.png"
                                    }
                                  ]
                                }
                              }
                            ]
                          },
                          "usage": {
                            "height": 928,
                            "image_count": 1,
                            "width": 1664
                          },
                          "request_id": "request-1"
                        }
                        """, MediaType.APPLICATION_JSON));

        ImageRequest request = ImageRequest.builder()
                .position(4)
                .sectionTitle("AI Cover")
                .prompt("Create a modern cover image")
                .build();

        ImageAsset asset = aiImageGenerationService.acquire(request);

        assertThat(asset).isNotNull();
        assertThat(asset.getMethod()).isEqualTo(ImageMethodEnum.AI_GENERATION);
        assertThat(asset.getMediaType()).isEqualTo(ImageMediaTypeEnum.IMAGE_URL);
        assertThat(asset.getUrl()).isEqualTo("https://dashscope-result.example.com/generated.png");
        assertThat(asset.getContentType()).isEqualTo("image/png");
        assertThat(asset.getFileName()).isEqualTo("ai-generated-4.png");
        assertThat(asset.getStorageFolder()).isEqualTo("ai-generated");
        assertThat(asset.getDescription()).isEqualTo("Create a modern cover image");
        mockServer.verify();
    }

    @Test
    void shouldNotSupportRequestWhenApiKeyIsBlank() {
        AiImageProperties properties = new AiImageProperties();
        properties.setApiKey(" ");
        AiImageGenerationServiceImpl service = new AiImageGenerationServiceImpl(properties, new ObjectMapper());

        ImageRequest request = ImageRequest.builder()
                .prompt("Create a modern cover image")
                .build();

        assertThat(service.supports(request)).isFalse();
    }

    @Test
    void shouldReturnNullWhenDashScopeReturnsError() {
        mockServer.expect(requestTo("https://dashscope.example.com/api/v1/services/aigc/multimodal-generation/generation"))
                .andRespond(withSuccess("""
                        {
                          "request_id": "request-2",
                          "code": "InvalidParameter",
                          "message": "invalid size"
                        }
                        """, MediaType.APPLICATION_JSON));

        ImageRequest request = ImageRequest.builder()
                .position(1)
                .prompt("Create a modern cover image")
                .build();

        assertThat(aiImageGenerationService.acquire(request)).isNull();
        mockServer.verify();
    }
}
