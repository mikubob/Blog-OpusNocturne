package com.xuan.service.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuan.common.exceptions.BusinessException;
import com.xuan.common.utils.PasswordUtils;
import com.xuan.entity.dto.user.UserCreateDTO;
import com.xuan.entity.dto.user.UserPageQueryDTO;
import com.xuan.entity.dto.user.UserResetPasswordDTO;
import com.xuan.entity.dto.user.UserUpdateDTO;
import com.xuan.entity.po.sys.SysUser;
import com.xuan.entity.po.sys.SysUserRole;
import com.xuan.entity.vo.auth.UserInfoVO;
import com.xuan.entity.vo.user.UserListVO;
import com.xuan.service.mapper.SysPermissionMapper;
import com.xuan.service.mapper.SysUserMapper;
import com.xuan.service.mapper.SysUserRoleMapper;
import com.xuan.service.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.xuan.common.enums.ErrorCode.USER_EXISTS;
import static com.xuan.common.enums.ErrorCode.USER_NOT_FOUND;

/**
 * 用户管理服务实现类
 *
 * @author 玄〤
 * @since 2026-02-20
 */
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {

    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysPermissionMapper sysPermissionMapper;

    /**
     * 分页获取用户列表
     *
     * @param dto 查询参数
     * @return 分页用户列表
     */
    @Override
    public Page<UserListVO> pageUsers(UserPageQueryDTO dto) {
        //1. 填充查询条件
        LambdaQueryWrapper<SysUser> wrapper=new LambdaQueryWrapper<>();
        if (dto.getUsername()!=null&& !dto.getUsername().trim().isEmpty()){
            wrapper.like(SysUser::getUsername,dto.getUsername());
        }
        if (dto.getNickname()!=null&& !dto.getNickname().trim().isEmpty()){
            wrapper.like(SysUser::getNickname,dto.getNickname());
        }

        wrapper.orderByDesc(SysUser::getCreateTime);

        //2. 分页查询
        //2.1 确保分页参数不为null，提供默认值
        int currentPage=dto.getCurrent()!=null?dto.getCurrent():1;
        int pageSize=dto.getSize()!=null?dto.getSize():10;
        //2.2 创建分页对象
        Page<SysUser> page=new Page<>(currentPage,pageSize);
        Page<UserListVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());

        //2.3 转换为VO对象
        voPage.setRecords(page.getRecords().stream()
                .map(user->{
                    return BeanUtil.copyProperties(user, UserListVO.class);
                }).collect(Collectors.toList()));
        //3. 返回分页结果
        return voPage;
    }

    /**
     * 创建用户
     * @param dto 创建用户参数
     */
    @Override
    @Transactional
    public void createUser(UserCreateDTO dto) {
        //1.检查用户名唯一
        long count = count(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername()));
        if (count>0){
            throw new BusinessException(USER_EXISTS);
        }
        //2.保存用户
        SysUser sysUser = BeanUtil.copyProperties(dto, SysUser.class);
        save(sysUser);

        //3.保存用户角色关联表
        saveUserRoles(sysUser.getId(),dto.getRoleIds());

    }

    /**
     * 更新用户
     * @param id 用户id
     * @param dto 更新参数
     */
    @Override
    @Transactional
    public void updateUser(Long id, UserUpdateDTO dto) {
        //1.查询用户是否存在
        SysUser sysUser = getById(id);
        if (sysUser==null){
            throw new BusinessException(USER_NOT_FOUND);
        }
        //2.设置更新数据
        if (dto.getNickname()!=null){
            sysUser.setNickname(dto.getNickname());
        }
        if (dto.getEmail()!=null){
            sysUser.setEmail(dto.getEmail());
        }
        if (dto.getStatus()!=null){
            sysUser.setStatus(dto.getStatus());
        }
        updateById(sysUser);

        //3.更新角色关联
        if (dto.getRoleIds()!=null){
            sysUserRoleMapper.deleteByUserId(id);
            saveUserRoles(id,dto.getRoleIds());
        }
    }

    /**
     * 删除用户
     * @param id 用户id
     */
    @Override
    @Transactional
    public void deleteUser(Long id) {
        //1.查询用户是否存在
        SysUser sysUser = getById(id);
        if (sysUser==null){
            throw new BusinessException(USER_NOT_FOUND);
        }
        //2.删除用户
        removeById(id);

        //3.删除用户角色关联
        sysUserRoleMapper.deleteByUserId(id);
    }

    /**
     * 获取用户详情
     * @param id 用户id
     * @return 用户详情
     */
    @Override
    public UserInfoVO getUserDetail(Long id) {
        //1.查询用户是否存在
        SysUser sysUser = getById(id);
        if (sysUser==null){
            throw new BusinessException(USER_NOT_FOUND);
        }
        //2.转换为VO对象
        UserInfoVO userInfoVO = BeanUtil.copyProperties(sysUser, UserInfoVO.class);
        //3.查询角色id列表并保存
        List<Long> roleIds = sysUserRoleMapper.selectRoleIdsByUserId(id);
        userInfoVO.setRoleIds(roleIds);
        //4.查询权限
        List<String> permissions = sysPermissionMapper.selectPermissionCodesByUserId(id);
        userInfoVO.setPermissions(permissions);
        return userInfoVO;
    }

    /**
     * 重置用户密码
     * @param id 用户id
     * @param dto 重置密码参数
     */
    @Override
    public void resetPassword(Long id, UserResetPasswordDTO dto) {
        //1.查询用户是否存在
        SysUser sysUser = getById(id);
        if (sysUser==null){
            throw new BusinessException(USER_NOT_FOUND);
        }
        //2.设置密码
        sysUser.setPassword(PasswordUtils.encode(dto.getPassword()));
        updateById(sysUser);
    }

    /**
     * 保存用户角色关联
     * @param userId 用户id
     * @param roleIds 角色id列表
     */
    private void saveUserRoles(Long userId, List<Long> roleIds){
        if (roleIds!=null && !roleIds.isEmpty()){
            LocalDateTime now=LocalDateTime.now();
            for (Long roleId:roleIds){
                SysUserRole sysUserRole=new SysUserRole();
                sysUserRole.setUserId(userId);
                sysUserRole.setRoleId(roleId);
                sysUserRole.setCreateTime(now);
                sysUserRoleMapper.insert(sysUserRole);
            }
        }
    }
}
