package com.xuan.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuan.entity.po.sys.SysPermission;
import com.xuan.service.mapper.SysPermissionMapper;
import com.xuan.service.service.ISysPermissionService;
import org.springframework.stereotype.Service;

/**
 * 权限服务实现类
 *
 * @author 玄〤
 * @since 2026-02-20
 */
@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements ISysPermissionService {
}