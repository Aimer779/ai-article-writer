package cn.nuist.aiarticlewriter.mapper;

import cn.nuist.aiarticlewriter.model.entity.Article;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Article mapper.
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {
}
