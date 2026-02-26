package com.xuan.service.service;

import com.xuan.entity.dto.auth.ChangePasswordDTO;
import com.xuan.entity.dto.auth.LoginDTO;
import com.xuan.entity.vo.auth.LoginVO;
import com.xuan.entity.vo.auth.UserInfoVO;

/**
 * 认证服务接口
 *
 * @author 玄〤
 * @since 2026-02-20
 */
public interface IAuthService {

    /**
     * 用户登录
     * @param loginDTO 登录参数
     * @return 登录结果
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 退出登录
     * @param username 用户名
     */
    void logout(String username);

    /**
     * 获取当前用户信息
     * @param username 用户名
     * @return 用户信息
     */
    UserInfoVO getUserInfo(String username);

    /**
     * 刷新Token
     * @param username 用户名
     * @return 新的Token
     */
    LoginVO refreshToken(String username);

    /**
     * 修改密码
     * @param userId 用户id
     * @param dto 修改密码参数
     */
    void changePassword(Long userId, ChangePasswordDTO dto);
}
