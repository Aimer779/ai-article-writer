package cn.nuist.aiarticlewriter.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.nuist.aiarticlewriter.constant.UserConstant;
import cn.nuist.aiarticlewriter.exception.ErrorCode;
import cn.nuist.aiarticlewriter.exception.ThrowUtils;
import cn.nuist.aiarticlewriter.mapper.AgentLogMapper;
import cn.nuist.aiarticlewriter.model.dto.agentlog.AgentLogQueryRequest;
import cn.nuist.aiarticlewriter.model.entity.AgentLog;
import cn.nuist.aiarticlewriter.model.entity.User;
import cn.nuist.aiarticlewriter.model.enums.AgentLogStatusEnum;
import cn.nuist.aiarticlewriter.model.vo.AgentExecutionStatsVO;
import cn.nuist.aiarticlewriter.model.vo.AgentLogVO;
import cn.nuist.aiarticlewriter.service.AgentLogService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Agent execution log service implementation.
 */
@Service
@Slf4j
public class AgentLogServiceImpl implements AgentLogService {

    private static final long MAX_PAGE_SIZE = 50;

    private static final Set<String> AGENT_LOG_SORT_FIELD_SET = Set.of(
            "id",
            "taskId",
            "agentName",
            "startTime",
            "endTime",
            "durationMs",
            "status",
            "createTime",
            "updateTime"
    );

    @Resource
    private AgentLogMapper agentLogMapper;

    @Override
    @Async("articleExecutor")
    public void saveLogAsync(AgentLog agentLog) {
        if (agentLog == null) {
            return;
        }
        try {
            LocalDateTime now = LocalDateTime.now();
            if (agentLog.getCreateTime() == null) {
                agentLog.setCreateTime(now);
            }
            agentLog.setUpdateTime(now);
            if (agentLog.getIsDelete() == null) {
                agentLog.setIsDelete(0);
            }
            if (StrUtil.isBlank(agentLog.getTaskId())) {
                agentLog.setTaskId("unknown");
            }
            agentLogMapper.insert(agentLog);
        } catch (Exception e) {
            log.warn("Failed to save agent execution log, agentName={}, taskId={}",
                    agentLog.getAgentName(), agentLog.getTaskId(), e);
        }
    }

    @Override
    public AgentLogVO getAgentLogVOById(long id, User loginUser) {
        validateAdmin(loginUser);
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR, "Agent log id is invalid");
        AgentLog agentLog = agentLogMapper.selectOneById(id);
        ThrowUtils.throwIf(agentLog == null, ErrorCode.NOT_FOUND_ERROR, "Agent log does not exist");
        return getAgentLogVO(agentLog);
    }

    @Override
    public Page<AgentLogVO> listAgentLogByPage(AgentLogQueryRequest request, User loginUser) {
        validateAdmin(loginUser);
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getPageNum() <= 0, ErrorCode.PARAMS_ERROR, "Page number is invalid");
        ThrowUtils.throwIf(request.getPageSize() <= 0 || request.getPageSize() > MAX_PAGE_SIZE,
                ErrorCode.PARAMS_ERROR, "Page size is invalid");

        String finalSortField = StrUtil.blankToDefault(request.getSortField(), "createTime");
        ThrowUtils.throwIf(!AGENT_LOG_SORT_FIELD_SET.contains(finalSortField), ErrorCode.PARAMS_ERROR,
                "Sort field is invalid");
        boolean isAsc = "ascend".equals(request.getSortOrder());

        QueryWrapper queryWrapper = buildQueryWrapper(request);
        queryWrapper.orderBy(finalSortField, isAsc);
        Page<AgentLog> agentLogPage = agentLogMapper.paginate(request.getPageNum(), request.getPageSize(),
                queryWrapper);
        return agentLogPage.map(this::getAgentLogVO);
    }

    @Override
    public AgentExecutionStatsVO getExecutionStats(AgentLogQueryRequest request, User loginUser) {
        validateAdmin(loginUser);
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);

        List<AgentLog> agentLogs = agentLogMapper.selectListByQuery(buildQueryWrapper(request));
        long totalCount = agentLogs.size();
        long successCount = agentLogs.stream()
                .filter(agentLog -> AgentLogStatusEnum.SUCCESS.getValue().equals(agentLog.getStatus()))
                .count();
        long failedCount = agentLogs.stream()
                .filter(agentLog -> AgentLogStatusEnum.FAILED.getValue().equals(agentLog.getStatus()))
                .count();
        IntSummaryStatistics durationStats = agentLogs.stream()
                .map(AgentLog::getDurationMs)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .summaryStatistics();

        AgentExecutionStatsVO statsVO = new AgentExecutionStatsVO();
        statsVO.setTotalCount(totalCount);
        statsVO.setSuccessCount(successCount);
        statsVO.setFailedCount(failedCount);
        statsVO.setSuccessRate(totalCount == 0 ? 0D : (double) successCount / totalCount);
        statsVO.setAverageDurationMs(durationStats.getCount() == 0 ? 0D : durationStats.getAverage());
        statsVO.setMaxDurationMs(durationStats.getCount() == 0 ? 0 : durationStats.getMax());
        statsVO.setMinDurationMs(durationStats.getCount() == 0 ? 0 : durationStats.getMin());
        return statsVO;
    }

    @Override
    public AgentLogVO getAgentLogVO(AgentLog agentLog) {
        if (agentLog == null) {
            return null;
        }
        AgentLogVO agentLogVO = new AgentLogVO();
        BeanUtils.copyProperties(agentLog, agentLogVO);
        return agentLogVO;
    }

    private QueryWrapper buildQueryWrapper(AgentLogQueryRequest request) {
        return QueryWrapper.create()
                .eq("id", request.getId(), request.getId() != null)
                .eq("taskId", request.getTaskId(), StrUtil.isNotBlank(request.getTaskId()))
                .eq("agentName", request.getAgentName(), StrUtil.isNotBlank(request.getAgentName()))
                .eq("status", request.getStatus(), StrUtil.isNotBlank(request.getStatus()))
                .ge("startTime", request.getStartTime(), request.getStartTime() != null)
                .le("startTime", request.getEndTime(), request.getEndTime() != null);
    }

    private void validateAdmin(User loginUser) {
        ThrowUtils.throwIf(loginUser == null || loginUser.getId() == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(!UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole()), ErrorCode.NO_AUTH_ERROR);
    }
}
