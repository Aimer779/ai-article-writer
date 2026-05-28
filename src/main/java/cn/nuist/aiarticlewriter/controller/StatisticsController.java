package cn.nuist.aiarticlewriter.controller;

import cn.nuist.aiarticlewriter.annotation.AuthCheck;
import cn.nuist.aiarticlewriter.common.BaseResponse;
import cn.nuist.aiarticlewriter.common.ResultUtils;
import cn.nuist.aiarticlewriter.constant.UserConstant;
import cn.nuist.aiarticlewriter.model.vo.StatisticsVO;
import cn.nuist.aiarticlewriter.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Statistics controller.
 */
@RestController
@RequestMapping("/statistics")
@Tag(name = "Statistics Controller")
public class StatisticsController {

    @Resource
    private StatisticsService statisticsService;

    /**
     * Get dashboard statistics.
     *
     * @return dashboard statistics
     */
    @GetMapping("/overview")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @Operation(summary = "Get dashboard statistics")
    public BaseResponse<StatisticsVO> getStatistics() {
        return ResultUtils.success(statisticsService.getStatistics());
    }
}
