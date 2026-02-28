package com.xuan.service.service;

import com.xuan.entity.vo.monitor.ServerMonitorVO;

/**
 * 系统监控服务接口
 */
public interface IMonitorService {
    /**
     * 获取服务器监控信息
     * 
     * @return 监控信息
     */
    ServerMonitorVO getServerInfo();
}
