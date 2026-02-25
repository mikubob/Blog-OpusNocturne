package com.xuan.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xuan.entity.dto.user.UserCreateDTO;
import com.xuan.entity.dto.user.UserPageQueryDTO;
import com.xuan.entity.dto.user.UserResetPasswordDTO;
import com.xuan.entity.dto.user.UserUpdateDTO;
import com.xuan.entity.po.sys.SysUser;
import com.xuan.entity.vo.auth.UserInfoVO;
import com.xuan.entity.vo.user.UserListVO;

/**
 * 用户管理服务接口
 */
public interface ISysUserService extends IService<SysUser> {

    /** 分页查询用户列表 */
    Page<UserListVO> pageUsers(UserPageQueryDTO dto);

    /** 创建用户 */
    void createUser(UserCreateDTO dto);

    /** 更新用户 */
    void updateUser(Long id, UserUpdateDTO dto);

    /** 删除用户 */
    void deleteUser(Long id);

    /** 获取用户详情 */
    UserInfoVO getUserDetail(Long id);

    /** 重置用户密码 */
    void resetPassword(Long id, UserResetPasswordDTO dto);
}