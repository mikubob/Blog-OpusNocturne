package com.xuan.service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuan.entity.po.sys.SysRole;
import com.xuan.entity.po.sys.SysRolePermission;
import com.xuan.service.mapper.SysPermissionMapper;
import com.xuan.service.mapper.SysRoleMapper;
import com.xuan.service.mapper.SysRolePermissionMapper;
import com.xuan.service.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色服务实现类
 *
 * @author 玄〤
 * @since 2026-02-20
 */
@RequiredArgsConstructor
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    private final SysPermissionMapper sysPermissionMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;

    /**
     * 分配角色权限权限
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     */
    @Override
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        //1.删除旧的权限关联
        sysRolePermissionMapper.deleteByRoleId(roleId);
        //2.批量插入新的权限关联
        if (permissionIds != null && !permissionIds.isEmpty()){
            LocalDateTime now=LocalDateTime.now();
            for (Long permissionId : permissionIds){
                SysRolePermission rp = SysRolePermission.builder()
                        .roleId(roleId)
                        .permissionId(permissionId)
                        .createTime(now)
                        .build();
                sysRolePermissionMapper.insert(rp);
            }
        }

    }

    /**
     * 获取角色权限ID列表
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    @Override
    public List<Long> getRolePermissionIds(Long roleId) {
        return sysPermissionMapper.selectPermissionIdsByRoleId(roleId);
    }
}
