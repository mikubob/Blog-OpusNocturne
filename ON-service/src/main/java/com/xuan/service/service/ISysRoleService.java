package com.xuan.service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xuan.entity.po.sys.SysRole;

import java.util.List;

/**
 * 角色服务接口
 */
public interface ISysRoleService extends IService<SysRole> {

    /** 分配权限 */
    void assignPermissions(Long roleId, List<Long> permissionIds);

    /** 获取角色的权限ID列表 */
    List<Long> getRolePermissionIds(Long roleId);
}