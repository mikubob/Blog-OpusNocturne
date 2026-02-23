package com.xuan.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuan.entity.po.blog.ArticleLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章点赞 Mapper
 */
@Mapper
public interface ArticleLikeMapper extends BaseMapper<ArticleLike> {
}
