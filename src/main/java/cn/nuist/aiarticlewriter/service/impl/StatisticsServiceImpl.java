package cn.nuist.aiarticlewriter.service.impl;

import cn.nuist.aiarticlewriter.mapper.ArticleMapper;
import cn.nuist.aiarticlewriter.mapper.UserMapper;
import cn.nuist.aiarticlewriter.model.entity.Article;
import cn.nuist.aiarticlewriter.model.enums.ArticleStatusEnum;
import cn.nuist.aiarticlewriter.model.enums.UserRoleEnum;
import cn.nuist.aiarticlewriter.model.vo.StatisticsVO;
import cn.nuist.aiarticlewriter.service.StatisticsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Objects;

/**
 * Statistics service implementation.
 */
@Service
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {

    private static final String CACHE_KEY = "statistics:overview";

    private static final Duration CACHE_TTL = Duration.ofMinutes(1);

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public StatisticsVO getStatistics() {
        StatisticsVO cachedStatistics = getCachedStatistics();
        if (cachedStatistics != null) {
            return cachedStatistics;
        }

        StatisticsVO statisticsVO = buildStatistics();
        cacheStatistics(statisticsVO);
        return statisticsVO;
    }

    private StatisticsVO buildStatistics() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
        LocalDateTime weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();

        long todayCount = countArticleCreatedBetween(todayStart, tomorrowStart);
        long weekCount = countArticleCreatedAfter(weekStart);
        long monthCount = countArticleCreatedAfter(monthStart);
        long totalCount = articleMapper.selectCountByQuery(QueryWrapper.create());
        long completedCount = articleMapper.selectCountByQuery(QueryWrapper.create()
                .eq("status", ArticleStatusEnum.COMPLETED.getValue()));
        long totalUserCount = userMapper.selectCountByQuery(QueryWrapper.create());
        long vipUserCount = userMapper.selectCountByQuery(QueryWrapper.create()
                .eq("userRole", UserRoleEnum.VIP.getValue()));
        long activeUserCount = articleMapper.selectListByQuery(QueryWrapper.create()
                        .select("userId")
                        .ge("createTime", weekStart)
                        .isNotNull("userId"))
                .stream()
                .map(Article::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        Integer avgDurationMs = calculateAverageDurationMs();

        return StatisticsVO.builder()
                .todayCount(todayCount)
                .weekCount(weekCount)
                .monthCount(monthCount)
                .totalCount(totalCount)
                .successRate(calculateSuccessRate(completedCount, totalCount))
                .avgDurationMs(avgDurationMs)
                .activeUserCount(activeUserCount)
                .totalUserCount(totalUserCount)
                .vipUserCount(vipUserCount)
                .quotaUsed(totalCount)
                .build();
    }

    private long countArticleCreatedBetween(LocalDateTime startTime, LocalDateTime endTime) {
        return articleMapper.selectCountByQuery(QueryWrapper.create()
                .ge("createTime", startTime)
                .lt("createTime", endTime));
    }

    private long countArticleCreatedAfter(LocalDateTime startTime) {
        return articleMapper.selectCountByQuery(QueryWrapper.create()
                .ge("createTime", startTime));
    }

    private Integer calculateAverageDurationMs() {
        List<Article> completedArticles = articleMapper.selectListByQuery(QueryWrapper.create()
                .select("createTime", "completedTime")
                .eq("status", ArticleStatusEnum.COMPLETED.getValue())
                .isNotNull("createTime")
                .isNotNull("completedTime"));
        double average = completedArticles.stream()
                .mapToLong(article -> Duration.between(article.getCreateTime(), article.getCompletedTime()).toMillis())
                .filter(durationMs -> durationMs >= 0)
                .average()
                .orElse(0D);
        return average > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) Math.round(average);
    }

    private Double calculateSuccessRate(long completedCount, long totalCount) {
        if (totalCount == 0) {
            return 0D;
        }
        double rate = (double) completedCount * 100 / totalCount;
        return Math.round(rate * 100D) / 100D;
    }

    private StatisticsVO getCachedStatistics() {
        try {
            String cachedValue = stringRedisTemplate.opsForValue().get(CACHE_KEY);
            if (cachedValue == null || cachedValue.isBlank()) {
                return null;
            }
            return objectMapper.readValue(cachedValue, StatisticsVO.class);
        } catch (Exception e) {
            log.warn("Failed to read statistics cache", e);
            return null;
        }
    }

    private void cacheStatistics(StatisticsVO statisticsVO) {
        try {
            stringRedisTemplate.opsForValue().set(CACHE_KEY, objectMapper.writeValueAsString(statisticsVO), CACHE_TTL);
        } catch (Exception e) {
            log.warn("Failed to write statistics cache", e);
        }
    }
}
