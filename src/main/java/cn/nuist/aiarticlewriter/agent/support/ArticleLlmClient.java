package cn.nuist.aiarticlewriter.agent.support;

import cn.nuist.aiarticlewriter.agent.ArticleAgentException;
import cn.nuist.aiarticlewriter.model.enums.SseMessageTypeEnum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Shared LangChain4j client for article agents.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleLlmClient {

    private final ChatModel chatModel;

    private final StreamingChatModel streamingChatModel;

    private final ObjectMapper objectMapper;

    /**
     * Call LLM without streaming.
     *
     * @param prompt prompt text
     * @return model output
     */
    public String callLlm(String prompt) {
        return chatModel.chat(prompt);
    }

    /**
     * Call LLM with streaming output.
     *
     * @param prompt prompt text
     * @param streamHandler stream handler
     * @param messageType SSE message type
     * @return full model output
     */
    public String callLlmWithStreaming(String prompt, Consumer<String> streamHandler, SseMessageTypeEnum messageType) {
        StringBuilder contentBuilder = new StringBuilder();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> errorRef = new AtomicReference<>();

        streamingChatModel.chat(prompt, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String partialResponse) {
                if (partialResponse != null && !partialResponse.isEmpty()) {
                    contentBuilder.append(partialResponse);
                    if (streamHandler != null) {
                        streamHandler.accept(messageType.getStreamingPrefix() + partialResponse);
                    }
                }
            }

            @Override
            public void onCompleteResponse(ChatResponse completeResponse) {
                latch.countDown();
            }

            @Override
            public void onError(Throwable error) {
                log.error("LLM streaming call failed, messageType={}", messageType, error);
                errorRef.set(error);
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ArticleAgentException("LLM streaming call was interrupted", e);
        }
        if (errorRef.get() != null) {
            throw new ArticleAgentException("LLM streaming call failed", errorRef.get());
        }
        return contentBuilder.toString();
    }

    /**
     * Parse JSON response into a concrete class.
     *
     * @param content JSON content
     * @param clazz target class
     * @param name response name
     * @param <T> target type
     * @return parsed object
     */
    public <T> T parseJsonResponse(String content, Class<T> clazz, String name) {
        try {
            return objectMapper.readValue(cleanJsonContent(content), clazz);
        } catch (JsonProcessingException e) {
            log.error("{} parse failed, content={}", name, content, e);
            throw new ArticleAgentException(name + " parse failed", e);
        }
    }

    /**
     * Parse JSON response into a generic type.
     *
     * @param content JSON content
     * @param typeReference target type reference
     * @param name response name
     * @param <T> target type
     * @return parsed object
     */
    public <T> T parseJsonListResponse(String content, TypeReference<T> typeReference, String name) {
        try {
            return objectMapper.readValue(cleanJsonContent(content), typeReference);
        } catch (JsonProcessingException e) {
            log.error("{} parse failed, content={}", name, content, e);
            throw new ArticleAgentException(name + " parse failed", e);
        }
    }

    /**
     * Replace prompt variables in {{name}} form.
     *
     * @param template prompt template
     * @param variables prompt variables
     * @return rendered prompt
     */
    public String renderPrompt(String template, Map<String, String> variables) {
        String prompt = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            prompt = prompt.replace("{{" + entry.getKey() + "}}", safeValue(entry.getValue()));
        }
        return prompt;
    }

    private String cleanJsonContent(String content) {
        if (content == null) {
            return "";
        }
        String cleaned = content.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceFirst("^```(?:json)?\\s*", "");
            cleaned = cleaned.replaceFirst("\\s*```$", "");
        }
        return cleaned.trim();
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }
}
