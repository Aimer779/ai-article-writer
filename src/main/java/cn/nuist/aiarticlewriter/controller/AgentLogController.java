package cn.nuist.aiarticlewriter.controller;

import cn.nuist.aiarticlewriter.annotation.AuthCheck;
import cn.nuist.aiarticlewriter.common.BaseResponse;
import cn.nuist.aiarticlewriter.common.ResultUtils;
import cn.nuist.aiarticlewriter.constant.UserConstant;
import cn.nuist.aiarticlewriter.exception.ErrorCode;
import cn.nuist.aiarticlewriter.exception.ThrowUtils;
import cn.nuist.aiarticlewriter.model.dto.agentlog.AgentLogQueryRequest;
import cn.nuist.aiarticlewriter.model.entity.User;
import cn.nuist.aiarticlewriter.model.vo.AgentExecutionStatsVO;
import cn.nuist.aiarticlewriter.model.vo.AgentLogVO;
import cn.nuist.aiarticlewriter.service.AgentLogService;
import cn.nuist.aiarticlewriter.service.UserService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Agent execution log controller.
 */
@RestController
@RequestMapping("/agent/log")
@Tag(name = "Agent Log Controller")
public class AgentLogController {

    @Resource
    private AgentLogService agentLogService;

    @Resource
    private UserService userService;

    /**
     * Get agent log by id.
     *
     * @param id agent log id
     * @param request HTTP request
     * @return agent log view object
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "Get agent log by id")
    public BaseResponse<AgentLogVO> getAgentLogById(@RequestParam Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        AgentLogVO agentLogVO = agentLogService.getAgentLogVOById(id, loginUser);
        return ResultUtils.success(agentLogVO);
    }

    /**
     * Page agent logs.
     *
     * @param agentLogQueryRequest query request
     * @param request HTTP request
     * @return agent log page
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "Page agent logs")
    public BaseResponse<Page<AgentLogVO>> listAgentLogByPage(
            @RequestBody AgentLogQueryRequest agentLogQueryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(agentLogQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Page<AgentLogVO> agentLogPage = agentLogService.listAgentLogByPage(agentLogQueryRequest, loginUser);
        return ResultUtils.success(agentLogPage);
    }

    /**
     * Get agent execution statistics.
     *
     * @param agentLogQueryRequest query request
     * @param request HTTP request
     * @return execution statistics
     */
    @PostMapping("/stats")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "Get agent execution statistics")
    public BaseResponse<AgentExecutionStatsVO> getExecutionStats(
            @RequestBody AgentLogQueryRequest agentLogQueryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(agentLogQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        AgentExecutionStatsVO statsVO = agentLogService.getExecutionStats(agentLogQueryRequest, loginUser);
        return ResultUtils.success(statsVO);
    }
}
