package cn.nuist.aiarticlewriter.model.dto.agentlog;

import cn.nuist.aiarticlewriter.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Agent execution log query request.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentLogQueryRequest extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private String taskId;

    private String agentName;

    private String status;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
