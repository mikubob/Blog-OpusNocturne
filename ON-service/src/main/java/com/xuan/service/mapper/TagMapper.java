package com.xuan.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuan.entity.po.blog.Tag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 标签 Mapper
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {
}