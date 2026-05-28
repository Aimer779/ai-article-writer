package cn.nuist.aiarticlewriter.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * Statistics dashboard view object.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Today's article creation count.
     */
    private Long todayCount;

    /**
     * Current week article creation count.
     */
    private Long weekCount;

    /**
     * Current month article creation count.
     */
    private Long monthCount;

    /**
     * Total article creation count.
     */
    private Long totalCount;

    /**
     * Success rate percentage.
     */
    private Double successRate;

    /**
     * Average duration in milliseconds.
     */
    private Integer avgDurationMs;

    /**
     * Active user count in current week.
     */
    private Long activeUserCount;

    /**
     * Total user count.
     */
    private Long totalUserCount;

    /**
     * VIP user count.
     */
    private Long vipUserCount;

    /**
     * Total quota usage.
     */
    private Long quotaUsed;
}
