package cn.nuist.aiarticlewriter.service;

import cn.nuist.aiarticlewriter.model.dto.agentlog.AgentLogQueryRequest;
import cn.nuist.aiarticlewriter.model.entity.AgentLog;
import cn.nuist.aiarticlewriter.model.entity.User;
import cn.nuist.aiarticlewriter.model.vo.AgentExecutionStatsVO;
import cn.nuist.aiarticlewriter.model.vo.AgentLogVO;
import com.mybatisflex.core.paginate.Page;

/**
 * Agent execution log service.
 */
public interface AgentLogService {

    /**
     * Save agent execution log asynchronously.
     *
     * @param agentLog agent log entity
     */
    void saveLogAsync(AgentLog agentLog);

    /**
     * Get agent log view object by id.
     *
     * @param id agent log id
     * @param loginUser current user
     * @return agent log view object
     */
    AgentLogVO getAgentLogVOById(long id, User loginUser);

    /**
     * Page agent log view objects.
     *
     * @param request query request
     * @param loginUser current user
     * @return agent log page
     */
    Page<AgentLogVO> listAgentLogByPage(AgentLogQueryRequest request, User loginUser);

    /**
     * Get agent execution statistics.
     *
     * @param request query request
     * @param loginUser current user
     * @return execution statistics
     */
    AgentExecutionStatsVO getExecutionStats(AgentLogQueryRequest request, User loginUser);

    /**
     * Convert entity to view object.
     *
     * @param agentLog agent log entity
     * @return agent log view object
     */
    AgentLogVO getAgentLogVO(AgentLog agentLog);
}
