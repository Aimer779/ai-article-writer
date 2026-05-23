package cn.nuist.aiarticlewriter.mapper;

import cn.nuist.aiarticlewriter.model.entity.User;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * User mapper.
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
