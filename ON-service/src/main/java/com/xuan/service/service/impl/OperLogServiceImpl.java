package com.xuan.service.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuan.entity.dto.log.OperLogQueryDTO;
import com.xuan.entity.po.sys.SysOperLog;
import com.xuan.entity.vo.log.OperLogVO;
import com.xuan.service.mapper.SysOperLogMapper;
import com.xuan.service.service.IOperLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 操作日志服务实现类
 * 
 * @author 玄〤
 * @since 2026-03-02
 */
@Service
@RequiredArgsConstructor
public class OperLogServiceImpl extends ServiceImpl<SysOperLogMapper, SysOperLog> implements IOperLogService {

    private final SysOperLogMapper operLogMapper;

    @Override
    public Page<OperLogVO> pageOperLogs(OperLogQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<SysOperLog> wrapper = new LambdaQueryWrapper<>();
        
        // 模块名称搜索
        if (queryDTO.getModule() != null && !queryDTO.getModule().isEmpty()) {
            wrapper.like(SysOperLog::getModule, queryDTO.getModule());
        }
        
        // 状态筛选
        if (queryDTO.getStatus() != null) {
            wrapper.eq(SysOperLog::getStatus, queryDTO.getStatus());
        }
        
        // 时间范围筛选
        if (queryDTO.getStartTime() != null && !queryDTO.getStartTime().isEmpty()) {
            LocalDateTime start = DateUtil.parseLocalDateTime(queryDTO.getStartTime());
            wrapper.ge(SysOperLog::getOperTime, start);
        }
        
        if (queryDTO.getEndTime() != null && !queryDTO.getEndTime().isEmpty()) {
            LocalDateTime end = DateUtil.parseLocalDateTime(queryDTO.getEndTime());
            wrapper.le(SysOperLog::getOperTime, end);
        }
        
        // 按操作时间倒序排序
        wrapper.orderByDesc(SysOperLog::getOperTime);
        
        // 分页参数
        int current = queryDTO.getCurrent() != null ? queryDTO.getCurrent() : 1;
        int size = queryDTO.getSize() != null ? queryDTO.getSize() : 10;
        
        // 分页查询
        Page<SysOperLog> page = page(new Page<>(current, size), wrapper);
        Page<OperLogVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        
        // 转换为VO并处理字段映射
        voPage.setRecords(page.getRecords().stream().map(this::toVO).toList());
        
        return voPage;
    }
    
    /**
     * 将SysOperLog转换为OperLogVO，处理字段映射
     * @param log 操作日志实体
     * @return 操作日志VO
     */
    private OperLogVO toVO(SysOperLog log) {
        OperLogVO vo = new OperLogVO();
        vo.setId(log.getId());
        vo.setModule(log.getModule()); // 模块标题
        vo.setOperation(getOperationDescription(log.getBusinessType(), log.getMethod())); // 操作描述
        vo.setOperator(log.getOperName()); // 操作人员
        vo.setIp(log.getOperIp()); // IP地址
        vo.setStatus(log.getStatus()); // 操作状态
        vo.setCostTime(log.getCostTime()); // 消耗时间
        vo.setCreateTime(log.getOperTime()); // 操作时间
        return vo;
    }
    
    /**
     * 根据业务类型和方法名生成操作描述
     * @param businessType 业务类型
     * @param method 方法名
     * @return 操作描述
     */
    private String getOperationDescription(String businessType, String method) {
        switch (businessType) {
            case "1":
                return "新增操作";
            case "2":
                return "修改操作";
            case "3":
                return "删除操作";
            default:
                return "其他操作";
        }
    }
}
