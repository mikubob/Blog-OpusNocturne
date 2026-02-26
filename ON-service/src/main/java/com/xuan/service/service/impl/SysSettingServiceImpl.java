package com.xuan.service.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuan.entity.dto.system.SystemSettingDTO;
import com.xuan.entity.po.sys.SysSetting;
import com.xuan.entity.vo.system.SystemSettingVO;
import com.xuan.service.mapper.SysSettingMapper;
import com.xuan.service.service.ISysSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.xuan.common.constant.RedisConstant.SYS_SETTING_CACHE_KEY;
import static com.xuan.common.constant.RedisConstant.SYS_SETTING_TTL_HOURS;

/**
 * 系统设置服务实现（DB + Redis 双层缓存，Cache Aside Pattern）
 * <p>
 * 读取策略：优先从 Redis 获取缓存，未命中则查询数据库并回填缓存
 * 写入策略：先更新数据库，再删除 Redis 缓存，下次读取时自动重建
 *
 * @author 玄〤
 * @since 2026-02-20
 */
@Service
@RequiredArgsConstructor
public class SysSettingServiceImpl extends ServiceImpl<SysSettingMapper, SysSetting> implements ISysSettingService {

    private final StringRedisTemplate redisTemplate;

    /**
     * 获取系统设置（优先读Redis，未命中读DB并回填Redis）
     * @return
     */
    @Override
    public SystemSettingVO getSettings() {
        //1.尝试从Redis中获取缓存
        String cached=redisTemplate.opsForValue().get(SYS_SETTING_CACHE_KEY);
        if (cached != null) {
            return JSON.parseObject(cached, SystemSettingVO.class);
        }
        //2.缓存未命中，查询数据库（取第一条记录作为全局设置）
        SysSetting setting = lambdaQuery().last("LIMIT 1").one();
        if (setting == null){
            return new SystemSettingVO();
        }

        //3.转换为VO
        SystemSettingVO systemSettingVO = BeanUtil.copyProperties(setting, SystemSettingVO.class);

        //4.回填Redis缓存
        redisTemplate.opsForValue().set(SYS_SETTING_CACHE_KEY,
                JSON.toJSONString(systemSettingVO),
                SYS_SETTING_TTL_HOURS,
                TimeUnit.HOURS);

        //5.返回
        return systemSettingVO;
    }

    @Override
    public void updateSettings(SystemSettingDTO dto) {
        //1.查询现有设置
        SysSetting setting = lambdaQuery().last("LIMIT 1").one();
        if (setting == null){
            setting = new SysSetting();
        }

        //2.将DTO中的数据复制到setting中
        if (dto.getSiteName() != null)
            setting.setSiteName(dto.getSiteName());
        if (dto.getSiteDescription() != null)
            setting.setSiteDescription(dto.getSiteDescription());
        if (dto.getSiteKeywords() != null)
            setting.setSiteKeywords(dto.getSiteKeywords());
        if (dto.getFooterText() != null)
            setting.setFooterText(dto.getFooterText());
        if (dto.getAdminEmail() != null)
            setting.setAdminEmail(dto.getAdminEmail());
        if (dto.getCommentAudit() != null)
            setting.setCommentAudit(dto.getCommentAudit() ? 1 : 0);
        if (dto.getArticlePageSize() != null)
            setting.setArticlePageSize(dto.getArticlePageSize());
        if (dto.getCommentPageSize() != null)
            setting.setCommentPageSize(dto.getCommentPageSize());
        if (dto.getAboutMe() != null)
            setting.setAboutMe(dto.getAboutMe());

        //3.保存或更新数据库
        saveOrUpdate(setting);

        //4.删除Redis缓存（下次读取的时候自动重建）
        redisTemplate.delete(SYS_SETTING_CACHE_KEY);
    }
}
