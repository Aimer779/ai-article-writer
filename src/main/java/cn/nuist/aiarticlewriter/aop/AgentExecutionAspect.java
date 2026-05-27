package cn.nuist.aiarticlewriter.aop;

import cn.nuist.aiarticlewriter.annotation.AgentExecution;
import cn.nuist.aiarticlewriter.model.entity.AgentLog;
import cn.nuist.aiarticlewriter.model.enums.AgentLogStatusEnum;
import cn.nuist.aiarticlewriter.model.state.article.ArticleState;
import cn.nuist.aiarticlewriter.model.state.article.TitleResult;
import cn.nuist.aiarticlewriter.service.AgentLogService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Aspect that records agent execution logs and performance data.
 */
@Aspect
@Component
@Slf4j
public class AgentExecutionAspect {

    private static final int MAX_ERROR_MESSAGE_LENGTH = 2000;

    @Resource
    private AgentLogService agentLogService;

    @Resource
    private ObjectMapper objectMapper;

    @Around("@annotation(agentExecution)")
    public Object aroundAgentExecution(ProceedingJoinPoint pjp, AgentExecution agentExecution) throws Throwable {
        long startTime = System.currentTimeMillis();
        LocalDateTime startDateTime = LocalDateTime.now();

        String taskId = extractTaskId(pjp);
        AgentLog agentLog = AgentLog.builder()
                .taskId(taskId)
                .agentName(agentExecution.value())
                .startTime(startDateTime)
                .status(AgentLogStatusEnum.RUNNING.getValue())
                .prompt(extractPrompt(pjp))
                .inputData(extractInputData(pjp))
                .build();

        try {
            Object result = pjp.proceed();
            agentLog.setStatus(AgentLogStatusEnum.SUCCESS.getValue());
            agentLog.setOutputData(extractOutputData(result));
            log.info("Agent execution succeeded, agentName={}, taskId={}, durationMs={}",
                    agentExecution.value(), taskId, elapsedMillis(startTime));
            return result;
        } catch (Throwable e) {
            agentLog.setStatus(AgentLogStatusEnum.FAILED.getValue());
            agentLog.setErrorMessage(truncateErrorMessage(e));
            log.error("Agent execution failed, agentName={}, taskId={}, error={}",
                    agentExecution.value(), taskId, e.getMessage(), e);
            throw e;
        } finally {
            agentLog.setEndTime(LocalDateTime.now());
            agentLog.setDurationMs(elapsedMillis(startTime));
            agentLogService.saveLogAsync(agentLog);
        }
    }

    private String extractTaskId(ProceedingJoinPoint pjp) {
        Object[] args = pjp.getArgs();
        if (args == null || args.length == 0) {
            return "unknown";
        }
        for (Object arg : args) {
            if (arg instanceof ArticleState state && state.getTaskId() != null) {
                return state.getTaskId();
            }
        }
        for (Object arg : args) {
            if (arg instanceof String value && !value.isBlank()) {
                return value;
            }
        }
        return "unknown";
    }

    private String extractInputData(ProceedingJoinPoint pjp) {
        try {
            Object[] args = pjp.getArgs();
            if (args == null || args.length == 0) {
                return null;
            }
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            String[] paramNames = signature.getParameterNames();
            Map<String, Object> inputMap = new HashMap<>();
            for (int i = 0; i < args.length && i < paramNames.length; i++) {
                Object value = simplifyValue(args[i]);
                if (value != null) {
                    inputMap.put(paramNames[i], value);
                }
            }
            return inputMap.isEmpty() ? null : objectMapper.writeValueAsString(inputMap);
        } catch (Exception e) {
            log.warn("Failed to extract agent input data", e);
            return null;
        }
    }

    private String extractOutputData(Object result) {
        try {
            Object value = simplifyValue(result);
            if (value == null) {
                return null;
            }
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.warn("Failed to extract agent output data", e);
            return null;
        }
    }

    private String extractPrompt(ProceedingJoinPoint pjp) {
        try {
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            Method method = signature.getMethod();
            return method.getDeclaringClass().getSimpleName() + "." + method.getName();
        } catch (Exception e) {
            return null;
        }
    }

    private Object simplifyValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String stringValue) {
            return Map.of("type", "String", "length", stringValue.length());
        }
        if (value instanceof Number || value instanceof Boolean) {
            return value;
        }
        if (value instanceof ArticleState state) {
            Map<String, Object> stateMap = new HashMap<>();
            stateMap.put("taskId", state.getTaskId());
            stateMap.put("userId", state.getUserId());
            stateMap.put("topic", state.getTopic());
            stateMap.put("currentStep", state.getCurrentStep() == null ? null : state.getCurrentStep().getValue());
            stateMap.put("status", state.getStatus() == null ? null : state.getStatus().getValue());
            stateMap.put("titleOptionsCount", state.getTitleOptions() == null ? 0 : state.getTitleOptions().size());
            stateMap.put("imageRequirementsCount",
                    state.getImageRequirements() == null ? 0 : state.getImageRequirements().size());
            stateMap.put("imagesCount", state.getImages() == null ? 0 : state.getImages().size());
            TitleResult title = state.getSelectedTitle() == null ? state.getTitle() : state.getSelectedTitle();
            if (title != null) {
                stateMap.put("mainTitle", title.getMainTitle());
            }
            return stateMap;
        }
        if (value instanceof List<?> list) {
            return Map.of("type", "List", "size", list.size());
        }
        return Map.of("type", value.getClass().getSimpleName());
    }

    private Integer elapsedMillis(long startTime) {
        long duration = System.currentTimeMillis() - startTime;
        return duration > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) duration;
    }

    private String truncateErrorMessage(Throwable e) {
        String errorMessage = e.getMessage() == null ? e.getClass().getName() : e.getMessage();
        if (errorMessage.length() <= MAX_ERROR_MESSAGE_LENGTH) {
            return errorMessage;
        }
        return errorMessage.substring(0, MAX_ERROR_MESSAGE_LENGTH);
    }
}
