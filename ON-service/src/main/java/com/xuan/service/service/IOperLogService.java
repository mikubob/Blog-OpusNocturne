package com.xuan.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuan.entity.dto.log.OperLogQueryDTO;
import com.xuan.entity.vo.log.OperLogVO;

/**
 * 操作日志服务接口
 * 
 * @author 玄〤
 * @since 2026-03-02
 */
public interface IOperLogService {

    /**
     * 分页查询操作日志
     * @param queryDTO 查询参数
     * @return 分页结果
     */
    Page<OperLogVO> pageOperLogs(OperLogQueryDTO queryDTO);
}
