package com.xuan.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.xuan.common.enums.UserStatusEnum;
import com.xuan.common.exceptions.BusinessException;
import com.xuan.common.utils.JwtUtils;
import com.xuan.common.utils.PasswordUtils;
import com.xuan.entity.dto.auth.ChangePasswordDTO;
import com.xuan.entity.dto.auth.LoginDTO;
import com.xuan.entity.po.sys.SysUser;
import com.xuan.entity.vo.auth.LoginVO;
import com.xuan.entity.vo.auth.UserInfoVO;
import com.xuan.service.mapper.SysPermissionMapper;
import com.xuan.service.mapper.SysUserMapper;
import com.xuan.service.service.IAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.xuan.common.constant.RedisConstant.TOKEN_KEY_PREFIX;
import static com.xuan.common.enums.ErrorCode.LOGIN_FAILED;
import static com.xuan.common.enums.ErrorCode.OLD_PASSWORD_ERROR;
import static com.xuan.common.enums.ErrorCode.PASSWORD_NOT_MATCH;
import static com.xuan.common.enums.ErrorCode.TOKEN_EXPIRED;
import static com.xuan.common.enums.ErrorCode.UNAUTHORIZED;
import static com.xuan.common.enums.ErrorCode.USER_DISABLED;
import static com.xuan.common.enums.ErrorCode.USER_NOT_FOUND;
import static com.xuan.common.enums.UserStatusEnum.ENABLED;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final SysUserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate stringRedisTemplate;
    private final SysUserMapper sysUserMapper;
    private final SysPermissionMapper sysPermissionMapper;

    /**
     * 用户登录
     * @param loginDTO 登录参数
     * @return 登录结果
     */
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        //1.根据用户名查找用户
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, loginDTO.getUsername())
        );
        if (user==null){
            throw new BusinessException(LOGIN_FAILED);
        }
        //2.校验密码
        if(!PasswordUtils.matches(loginDTO.getPassword(), user.getPassword())){
            throw new BusinessException(LOGIN_FAILED);
        }
        //3.检查用户状态
        if(!user.getStatus().equals(ENABLED.getCode())){
            throw new BusinessException(USER_DISABLED);
        }
        //4.生成token
        String token = jwtUtils.generateToken(user.getUsername(), Map.of("userId", user.getId()));

        //5.将token存入Redis中（用于验证登录状态）
        String redisKey = TOKEN_KEY_PREFIX + token;
        stringRedisTemplate.opsForValue()
                .set(redisKey,token,jwtUtils.getExpiration(), TimeUnit.SECONDS);
        //6.更新最后登录时间
        sysUserMapper.update(null,new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId,user.getId())
                .set(SysUser::getLastLoginTime, LocalDateTime.now()));
        //7.构建返回后的结果
        return LoginVO.builder()
                .token(token)
                .tokenHead(jwtUtils.getTokenPrefix())
                .build();
    }

    /**
     * 退出登录
     * @param username 用户名
     */
    @Override
    public void logout(String username) {
        // 从Redis中删除token
        stringRedisTemplate.delete(TOKEN_KEY_PREFIX + username);
    }

    /**
     * 获取当前用户信息
     * @param username 用户名
     * @return 用户信息
     */
    @Override
    public UserInfoVO getUserInfo(String username) {
        //1.查询用户信息
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username));
        if (user==null) {
            throw new BusinessException(UNAUTHORIZED);
        }

        //2.查询用户权限列表
        List<String> permissions = sysPermissionMapper.selectPermissionCodesByUserId(user.getId());

        //3.构建返回结果并返回
        return UserInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .permissions(permissions)
                .build();
    }

    /**
     * 刷新Token
     * @param username 用户名
     * @return 新的Token
     */
    @Override
    public LoginVO refreshToken(String username) {
        //1.查询用户信息
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username));
        if (user==null) {
            throw new BusinessException(TOKEN_EXPIRED);
        }

        //2.生成新的token
        String token = jwtUtils.generateToken(username, Map.of("userId", user.getId()));

        //3.将新的token存入Redis中
        stringRedisTemplate.opsForValue()
                .set(TOKEN_KEY_PREFIX + token, token, jwtUtils.getExpiration(), TimeUnit.SECONDS);

        //4.构建返回结果并且返回
        return LoginVO.builder()
                .token(token)
                .tokenHead(jwtUtils.getTokenPrefix())
                .build();
    }

    /**
     * 修改密码
     * @param userId 用户id
     * @param dto 修改密码参数
     */
    @Override
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        //1.判断两次输入的密码是否一致
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException(PASSWORD_NOT_MATCH);
        }
        //2.查询当前登录的用户是否存在
        SysUser user = userMapper.selectById(userId);
        if (user==null) {
            throw new BusinessException(USER_NOT_FOUND);
        }

        //3.使用 passwordUtils进行密码校验
        if (!PasswordUtils.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(OLD_PASSWORD_ERROR);
        }
        //4.对新密码进行加密并且保存
        sysUserMapper.update(null,new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId,userId)
                .set(SysUser::getPassword, PasswordUtils.encode(dto.getNewPassword())));

        //5.登出用户（删除Token），强制重新登录
        logout(user.getUsername());
    }
}
