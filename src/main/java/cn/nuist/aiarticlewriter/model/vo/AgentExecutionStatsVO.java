package cn.nuist.aiarticlewriter.model.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * Agent execution statistics view object.
 */
@Data
public class AgentExecutionStatsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long totalCount;

    private Long successCount;

    private Long failedCount;

    private Double successRate;

    private Double averageDurationMs;

    private Integer maxDurationMs;

    private Integer minDurationMs;
}
