package com.xuan.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuan.entity.po.sys.SysOperLog;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

/**
 * 操作日志 Mapper
 */
@Mapper
public interface SysOperLogMapper extends BaseMapper<SysOperLog> {

    /**
     * 删除指定时间之前的操作日志
     * @param operTime 操作时间
     * @return 删除的记录数
     */
    int deleteByOperTimeBefore(LocalDateTime operTime);
}

