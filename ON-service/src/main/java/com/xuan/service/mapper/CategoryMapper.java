package com.xuan.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuan.entity.po.blog.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分类 Mapper
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}