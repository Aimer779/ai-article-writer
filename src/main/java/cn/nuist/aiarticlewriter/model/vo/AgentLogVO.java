package cn.nuist.aiarticlewriter.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Agent execution log view object.
 */
@Data
public class AgentLogVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String taskId;

    private String agentName;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer durationMs;

    private String status;

    private String errorMessage;

    private String prompt;

    private String inputData;

    private String outputData;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
