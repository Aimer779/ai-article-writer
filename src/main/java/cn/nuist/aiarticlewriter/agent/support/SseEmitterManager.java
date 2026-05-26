package cn.nuist.aiarticlewriter.agent.support;

import cn.nuist.aiarticlewriter.constant.ArticleConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages SSE connections by article generation task id.
 */
@Component
@Slf4j
public class SseEmitterManager {

    private final Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    /**
     * Create an SSE emitter for a task.
     *
     * @param taskId article generation task id
     * @return SSE emitter
     */
    public SseEmitter createEmitter(String taskId) {
        SseEmitter emitter = new SseEmitter(ArticleConstant.SSE_TIMEOUT_MS);

        emitter.onTimeout(() -> {
            log.warn("SSE connection timed out, taskId={}", taskId);
            emitterMap.remove(taskId);
        });
        emitter.onCompletion(() -> {
            log.info("SSE connection completed, taskId={}", taskId);
            emitterMap.remove(taskId);
        });
        emitter.onError(error -> {
            log.error("SSE connection failed, taskId={}", taskId, error);
            emitterMap.remove(taskId);
        });

        emitterMap.put(taskId, emitter);
        log.info("SSE connection created, taskId={}", taskId);
        return emitter;
    }

    /**
     * Send a message to the SSE emitter bound to a task.
     *
     * @param taskId article generation task id
     * @param message message content
     */
    public void send(String taskId, String message) {
        SseEmitter emitter = emitterMap.get(taskId);
        if (emitter == null) {
            log.warn("SSE emitter does not exist, taskId={}", taskId);
            return;
        }

        try {
            emitter.send(SseEmitter.event()
                    .data(message)
                    .reconnectTime(ArticleConstant.SSE_RECONNECT_TIME_MS));
            log.debug("SSE message sent, taskId={}, message={}", taskId, message);
        } catch (IOException e) {
            log.error("Failed to send SSE message, taskId={}", taskId, e);
            emitterMap.remove(taskId);
        }
    }

    /**
     * Complete the SSE emitter bound to a task.
     *
     * @param taskId article generation task id
     */
    public void complete(String taskId) {
        SseEmitter emitter = emitterMap.get(taskId);
        if (emitter == null) {
            log.warn("SSE emitter does not exist, taskId={}", taskId);
            return;
        }

        try {
            emitter.complete();
            log.info("SSE connection completed manually, taskId={}", taskId);
        } catch (Exception e) {
            log.error("Failed to complete SSE connection, taskId={}", taskId, e);
        } finally {
            emitterMap.remove(taskId);
        }
    }

    /**
     * Complete the SSE emitter with an error.
     *
     * @param taskId article generation task id
     * @param error error to send through emitter completion
     */
    public void completeWithError(String taskId, Throwable error) {
        SseEmitter emitter = emitterMap.get(taskId);
        if (emitter == null) {
            log.warn("SSE emitter does not exist, taskId={}", taskId);
            return;
        }

        try {
            emitter.completeWithError(error);
            log.info("SSE connection completed with error, taskId={}", taskId);
        } catch (Exception e) {
            log.error("Failed to complete SSE connection with error, taskId={}", taskId, e);
        } finally {
            emitterMap.remove(taskId);
        }
    }

    /**
     * Check whether an SSE emitter exists for the task.
     *
     * @param taskId article generation task id
     * @return true if the emitter exists
     */
    public boolean exists(String taskId) {
        return emitterMap.containsKey(taskId);
    }
}
