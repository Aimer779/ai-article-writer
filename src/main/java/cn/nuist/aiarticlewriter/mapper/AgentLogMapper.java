package cn.nuist.aiarticlewriter.mapper;

import cn.nuist.aiarticlewriter.model.entity.AgentLog;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Agent execution log mapper.
 */
@Mapper
public interface AgentLogMapper extends BaseMapper<AgentLog> {
}
