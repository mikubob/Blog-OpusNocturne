package com.xuan.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuan.entity.po.blog.ArticleTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章-标签关联 Mapper
 */
@Mapper
public interface ArticleTagMapper extends BaseMapper<ArticleTag> {

    /**
     * 批量插入文章标签关联
     */
    void batchInsertArticleTags(@Param("list") List<ArticleTag> articleTags);

    /**
     * 根据文章ID删除标签关联
     */
    void deleteByArticleId(@Param("articleId") Long articleId);

    /**
     * 根据文章ID查询标签ID列表
     */
    List<Long> selectTagIdsByArticleId(@Param("articleId") Long articleId);
}
