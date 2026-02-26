package com.xuan.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuan.entity.dto.system.SystemSettingDTO;
import com.xuan.entity.po.sys.SysSetting;
import com.xuan.entity.vo.system.SystemSettingVO;

/**
 * 系统设置服务接口（数据库持久化 + Redis 缓存）
 *
 * @author 玄〤
 * @since 2026-02-20
 */
public interface ISysSettingService extends IService<SysSetting> {

    /** 获取系统设置（优先读 Redis，缓存未命中读 DB） */
    SystemSettingVO getSettings();

    /** 更新系统设置（写 DB + 刷新 Redis 缓存） */
    void updateSettings(SystemSettingDTO dto);
}