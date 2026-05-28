package cn.nuist.aiarticlewriter.service;

import cn.nuist.aiarticlewriter.model.vo.StatisticsVO;

/**
 * Statistics service.
 */
public interface StatisticsService {

    /**
     * Get dashboard statistics.
     *
     * @return dashboard statistics
     */
    StatisticsVO getStatistics();
}
