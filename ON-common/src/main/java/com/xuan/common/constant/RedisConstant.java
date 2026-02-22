package com.xuan.common.constant;

/**
 * Redis 缓存常量类
 * 集中管理所有 Redis Key 前缀和缓存配置，避免硬编码分散在各个 Service 中
 *
 * @author 玄〤
 * @since 2026-02-20
 */
public final class RedisConstant {

    private RedisConstant() {
        // 禁止实例化
    }

    // ==================== Token 相关 ====================

    /** 用户 Token 缓存前缀，完整 Key: token:{username} */
    public static final String TOKEN_KEY_PREFIX = "token:";
    /** Token 默认过期时间（秒），与 JWT 过期时间保持一致 */
    public static final long TOKEN_TTL_SECONDS = 7200;

    // ==================== 系统设置相关 ====================

    /** 系统设置缓存 Key */
    public static final String SYS_SETTING_CACHE_KEY = "sys:setting:cache";
    /** 系统设置缓存过期时间（小时） */
    public static final long SYS_SETTING_TTL_HOURS = 24;

    // ==================== 访问统计相关 ====================

    /** 今日 PV 计数 Key 前缀，完整 Key: stats:pv:{yyyy-MM-dd} */
    public static final String STATS_PV_KEY_PREFIX = "stats:pv:";
    /** 今日 UV HyperLogLog Key 前缀，完整 Key: stats:uv:{yyyy-MM-dd} */
    public static final String STATS_UV_KEY_PREFIX = "stats:uv:";
    /** 全站总 PV Key */
    public static final String STATS_TOTAL_PV_KEY = "stats:total:pv";
    /** 访问统计数据过期时间（天） */
    public static final long STATS_TTL_DAYS = 7;

    // ==================== 文章相关缓存 ====================

    /** 文章浏览量 Redis Key 前缀，完整 Key: article:view:{articleId} */
    public static final String ARTICLE_VIEW_KEY_PREFIX = "article:view:";
    /** 文章详情缓存 Key 前缀，完整 Key: article:detail:{articleId} */
    public static final String ARTICLE_DETAIL_KEY_PREFIX = "article:detail:";
    /** 文章详情缓存过期时间（分钟） */
    public static final long ARTICLE_DETAIL_TTL_MINUTES = 30;

    /** 文章点赞数缓存 Key 前缀，完整 Key: article:like:count:{articleId} */
    public static final String ARTICLE_LIKE_COUNT_KEY_PREFIX = "article:like:count:";

    /** 用户是否点赞缓存 Key 前缀，完整 Key: article:like:user:{articleId}:{ip} */
    public static final String ARTICLE_USER_LIKE_KEY_PREFIX = "article:like:user:";

    // ==================== 分类 & 标签缓存 ====================

    /** 前台分类列表缓存 Key */
    public static final String CATEGORY_LIST_KEY = "blog:category:list";
    /** 前台标签列表缓存 Key */
    public static final String TAG_LIST_KEY = "blog:tag:list";
    /** 分类/标签列表缓存过期时间（小时） */
    public static final long CATEGORY_TAG_TTL_HOURS = 2;

    // ==================== 权限缓存 ====================

    /** 用户权限缓存 Key 前缀，完整 Key: user:perm:{userId} */
    public static final String USER_PERMISSION_KEY_PREFIX = "user:perm:";
    /** 用户权限缓存过期时间（小时） */
    public static final long USER_PERMISSION_TTL_HOURS = 1;

    // ==================== 验证码相关 ====================

    /** 邮箱验证码缓存前缀，完整 Key: captcha:{type}:{email} */
    public static final String CAPTCHA_KEY_PREFIX = "captcha:";
    /** 验证码过期时间（分钟） */
    public static final long CAPTCHA_TTL_MINUTES = 5;
}
