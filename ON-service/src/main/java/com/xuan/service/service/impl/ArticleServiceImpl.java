package com.xuan.service.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuan.common.constant.DateTimeFormatConstant;
import com.xuan.common.constant.RedisConstant;
import com.xuan.common.exceptions.BusinessException;
import com.xuan.common.utils.DateTimeFormatUtils;
import com.xuan.entity.dto.article.ArticleAdminPageQueryDTO;
import com.xuan.entity.dto.article.ArticleCreateDTO;
import com.xuan.entity.dto.article.ArticlePageQueryDTO;
import com.xuan.entity.dto.article.ArticleStatusDTO;
import com.xuan.entity.dto.article.ArticleTopDTO;
import com.xuan.entity.dto.article.ArticleUpdateDTO;
import com.xuan.entity.po.blog.Article;
import com.xuan.entity.po.blog.ArticleLike;
import com.xuan.entity.po.blog.ArticleTag;
import com.xuan.entity.po.blog.Category;
import com.xuan.entity.po.blog.Tag;
import com.xuan.entity.po.sys.SysUser;
import com.xuan.entity.vo.article.ArchiveVO;
import com.xuan.entity.vo.article.ArticleAdminDetailVO;
import com.xuan.entity.vo.article.ArticleAdminListVO;
import com.xuan.entity.vo.article.ArticleCreatVO;
import com.xuan.entity.vo.article.ArticleDetailVO;
import com.xuan.entity.vo.article.ArticleListVO;
import com.xuan.entity.vo.tag.TagVO;
import com.xuan.service.mapper.ArticleLikeMapper;
import com.xuan.service.mapper.ArticleMapper;
import com.xuan.service.mapper.ArticleTagMapper;
import com.xuan.service.mapper.CategoryMapper;
import com.xuan.service.mapper.SysUserMapper;
import com.xuan.service.mapper.TagMapper;
import com.xuan.service.service.IArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.xuan.common.constant.DateTimeFormatConstant.DAY_FORMAT_PATTERN;
import static com.xuan.common.constant.RedisConstant.ARTICLE_DETAIL_KEY_PREFIX;
import static com.xuan.common.constant.RedisConstant.ARTICLE_DETAIL_TTL_MINUTES;
import static com.xuan.common.constant.RedisConstant.ARTICLE_LIKE_COUNT_KEY_PREFIX;
import static com.xuan.common.constant.RedisConstant.ARTICLE_PAGE_KEY_PREFIX;
import static com.xuan.common.constant.RedisConstant.ARTICLE_PAGE_TTL_MINUTES;
import static com.xuan.common.constant.RedisConstant.ARTICLE_USER_LIKE_KEY_PREFIX;
import static com.xuan.common.constant.RedisConstant.ARTICLE_VIEW_KEY_PREFIX;
import static com.xuan.common.constant.RedisConstant.CATEGORY_LIST_KEY;
import static com.xuan.common.constant.RedisConstant.TAG_LIST_KEY;
import static com.xuan.common.enums.ArticleStatusEnum.PUBLISHED;
import static com.xuan.common.enums.ErrorCode.ARTICLE_CREATE_FAILED;
import static com.xuan.common.enums.ErrorCode.ARTICLE_DELETE_EMPTY;
import static com.xuan.common.enums.ErrorCode.ARTICLE_NOT_FOUND;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements IArticleService {

    private final StringRedisTemplate redisTemplate;
    private final ArticleLikeMapper articleLikeMapper;
    private final ArticleTagMapper articleTagMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final SysUserMapper sysUserMapper;

    /**
     * 创建文章
     *
     * @param articleCreateDTO 文章创建DTO
     * @return 文章创建VO
     */
    @Override
    @Transactional
    public ArticleCreatVO createArticle(ArticleCreateDTO articleCreateDTO) {
        // 1.转换并且保存文章
        Article article = BeanUtil.copyProperties(articleCreateDTO, Article.class);

        // 处理slug：如果用户未提供，则自动生成
        if (StrUtil.isBlank(article.getSlug())) {
            String generatedSlug = generateSlugFromTitle(article.getTitle());
            article.setSlug(ensureUniqueSlug(generatedSlug));
        } else {
            // 如果用户提供了slug，确保其唯一性
            article.setSlug(ensureUniqueSlug(article.getSlug()));
        }

        if (article.getStatus().equals(PUBLISHED)) {
            // 如果文章状态为发布，则设置发布时间为当前时间
            article.setPublishTime(LocalDateTime.now());
        }

        // 设置浏览次数为0
        article.setViewCount(0L);

        // 保存文章
        boolean saved = save(article);

        // 判断文章是否保存成功
        if (!saved) {
            // 保存失败,抛出错误信息
            throw new BusinessException(ARTICLE_CREATE_FAILED);
        }

        // 2.处理标签关联
        saveArticleTags(article.getId(), articleCreateDTO.getTagIds());
        // 3.清除分类/标签缓存（文章数量发生变化）
        clearCategoryTagCache();
        // 4.清除文章列表缓存
        clearArticlePageCache();

        // 5.返回文章创建VO
        return BeanUtil.copyProperties(article, ArticleCreatVO.class);
    }

    /**
     * 后台文章详情
     *
     * @param id 文章id
     * @return 文章详情VO
     */
    @Override
    public ArticleAdminDetailVO getArticleDetail(Long id) {
        // 1.查询文章,并做非空判断
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException(ARTICLE_NOT_FOUND);
        }

        // 2.转换为VO
        ArticleAdminDetailVO articleAdminDetailVO = BeanUtil.copyProperties(article, ArticleAdminDetailVO.class);

        // 3.填充分类名称
        if (article.getCategoryId() != null) {
            Category category = categoryMapper.selectById(article.getCategoryId());
            if (category != null) {
                articleAdminDetailVO.setCategoryName(category.getName());
            }
        }
        // 4.填充标签信息
        List<Long> tagIds = articleTagMapper.selectTagIdsByArticleId(id);
        articleAdminDetailVO.setTagIds(tagIds);
        if (tagIds != null && !tagIds.isEmpty()) {
            List<Tag> tags = tagMapper.selectBatchIds(tagIds);
            articleAdminDetailVO.setTags(tags.stream()
                    .map(this::toTagVO)
                    .toList());
        } else {
            articleAdminDetailVO.setTags(Collections.emptyList());
        }

        // 5.返回文章详情VO
        return articleAdminDetailVO;
    }

    /**
     * 后台文章列表
     *
     * @param articleAdminPageQueryDTO 查询参数
     * @return 分页列表
     */
    @Override
    public Page<ArticleAdminListVO> pageAdminArticles(ArticleAdminPageQueryDTO articleAdminPageQueryDTO) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        // 1. 根据标题模糊查询
        if (articleAdminPageQueryDTO.getTitle() != null && !articleAdminPageQueryDTO.getTitle().isEmpty()) {
            wrapper.like(Article::getTitle, articleAdminPageQueryDTO.getTitle());
        }
        // 2. 根据分类id查询
        if (articleAdminPageQueryDTO.getCategoryId() != null) {
            wrapper.eq(Article::getCategoryId, articleAdminPageQueryDTO.getCategoryId());
        }
        // 3. 根据状态查询
        if (articleAdminPageQueryDTO.getStatus() != null) {
            wrapper.eq(Article::getStatus, articleAdminPageQueryDTO.getStatus());
        }
        // 4. 根据创建时间倒序排序
        wrapper.orderByDesc(Article::getCreateTime);
        // 5. 分页查询
        // 5.1确保分页参数不为null，提供默认值
        int current = articleAdminPageQueryDTO.getCurrent() != null ? articleAdminPageQueryDTO.getCurrent() : 1;
        int size = articleAdminPageQueryDTO.getSize() != null ? articleAdminPageQueryDTO.getSize() : 10;
        // 5.2 创建分页对象
        Page<Article> page = page(new Page<>(current, size), wrapper);
        Page<ArticleAdminListVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        // 5.3 将分页对象中的实体类转换为VO类，并且添加实时浏览量，分类名称，作者昵称
        voPage.setRecords(page.getRecords()// 获取原始数据
                .stream()
                .map(article -> {
                    ArticleAdminListVO articleAdminListVO = BeanUtil.copyProperties(article, ArticleAdminListVO.class);// 转换为VO
                    // 从Redis中获取实时浏览量
                    articleAdminListVO.setViewCount(getViewCountFromRedis(article.getId(), article.getViewCount()));
                    // 填充分类名称
                    if (article.getCategoryId() != null) {
                        Category category = categoryMapper.selectById(article.getCategoryId());
                        if (category != null) {
                            articleAdminListVO.setCategoryName(category.getName());
                        }
                    }
                    // 填充作者昵称
                    if (article.getAuthorId() != null) {
                        SysUser user = sysUserMapper.selectById(article.getAuthorId());
                        if (user != null) {
                            articleAdminListVO.setAuthorNickname(user.getNickname());
                        }
                    }
                    return articleAdminListVO;
                }).collect(Collectors.toList()));
        // 6. 返回分页列表
        return voPage;
    }

    /**
     * 更新文章
     *
     * @param id               文章id
     * @param articleUpdateDTO 更新参数
     */
    @Override
    @Transactional
    public void updateArticle(Long id, ArticleUpdateDTO articleUpdateDTO) {
        // 1.查询文章并做非空判断
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException(ARTICLE_NOT_FOUND);
        }

        // 保存原始slug，用于判断是否需要检查唯一性
        String originalSlug = article.getSlug();

        // 2.更新文章基本信息
        BeanUtil.copyProperties(articleUpdateDTO, article, "id");

        // 处理slug：如果slug有变化，则需要确保唯一性
        if (!StrUtil.equals(article.getSlug(), originalSlug)) {
            if (StrUtil.isBlank(article.getSlug())) {
                // 如果用户清空了slug，则根据标题重新生成
                String generatedSlug = generateSlugFromTitle(article.getTitle());
                article.setSlug(ensureUniqueSlug(generatedSlug));
            } else {
                // 如果用户修改了slug，确保其唯一性
                article.setSlug(ensureUniqueSlug(article.getSlug()));
            }
        }

        if (article.getStatus().equals(PUBLISHED) && article.getPublishTime() == null) {
            article.setPublishTime(LocalDateTime.now());
        }
        updateById(article);

        // 3.更新标签关联：先删除旧的，再插入新的
        articleTagMapper.deleteByArticleId(id);
        saveArticleTags(id, articleUpdateDTO.getTagIds());
        // 4.清除文章详情缓存、分类/标签列表缓存和文章列表缓存
        clearArticleDetailCache(id);
        clearCategoryTagCache();
        clearArticlePageCache();

    }

    /**
     * 删除文章
     *
     * @param id 文章id
     */
    @Override
    @Transactional
    public void deleteArticle(Long id) {
        // 1.查询文章并做非空判断
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException(ARTICLE_NOT_FOUND);
        }
        // 2.删除文章
        removeById(id);
        // 3.删除标签关联
        articleTagMapper.deleteByArticleId(id);
        // 4.清除文章详情缓存、分类/标签列表缓存和文章列表缓存
        clearArticleDetailCache(id);
        clearCategoryTagCache();
        clearArticlePageCache();

        // 5.删除文章实时浏览量缓存
        redisTemplate.delete(ARTICLE_VIEW_KEY_PREFIX + id);
    }

    /**
     * 批量删除文章
     *
     * @param ids 文章id集合
     */
    @Override
    @Transactional
    public void batchDeleteArticle(List<Long> ids) {
        // 1.判断ids是否为空
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ARTICLE_DELETE_EMPTY);
        }

        // 2.检查文章是否存在
        List<Article> articles = listByIds(ids);
        List<Long> existingIds = articles.stream()
                .map(Article::getId)
                .toList();
        if (existingIds.isEmpty()) {
            throw new BusinessException(ARTICLE_NOT_FOUND);
        }

        // 3.找出不存在的文章id
        List<Long> notExistingIds = ids.stream()
                .filter(id -> !existingIds.contains(id))
                .toList();
        if (!notExistingIds.isEmpty()) {
            throw new BusinessException("文章【" + notExistingIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(",")) + "】不存在或者已被删除");
        }

        // 4.批量删除文章及其关联的标签
        removeBatchByIds(ids);
        for (Long id : ids) {

            articleTagMapper.deleteByArticleId(id);

            // 清除文章详情缓存
            clearArticleDetailCache(id);
            redisTemplate.delete(ARTICLE_VIEW_KEY_PREFIX + id);
        }

        // 5.清除分类/标签列表缓存和文章列表缓存
        clearCategoryTagCache();
        clearArticlePageCache();
    }

    /**
     * 文章置顶/取消置顶
     *
     * @param id            文章id
     * @param articleTopDTO 置顶参数
     */
    @Override
    @Transactional
    public void updateArticleTop(Long id, ArticleTopDTO articleTopDTO) {
        // 1.查询文章并做非空判断
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException(ARTICLE_NOT_FOUND);
        }

        // 2.更新文章置顶状态
        article.setIsTop(articleTopDTO.getIsTop());
        updateById(article);
        // 3.清除文章列表缓存（置顶状态影响排序）
        clearArticlePageCache();
    }

    /**
     * 更新文章状态
     *
     * @param id               文章id
     * @param articleStatusDTO 状态参数
     */
    @Override
    public void updateArticleStatus(Long id, ArticleStatusDTO articleStatusDTO) {
        // 1.查询文章并做非空判断
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException(ARTICLE_NOT_FOUND);
        }
        // 2.更新文章状态
        article.setStatus(articleStatusDTO.getStatus());
        // 如果文章状态为发布，则设置发布时间
        if (article.getStatus().equals(PUBLISHED) && article.getPublishTime() == null) {
            article.setPublishTime(LocalDateTime.now());
        }
        updateById(article);
        // 3.清除文章详情缓存和文章列表缓存
        clearArticleDetailCache(id);
        clearArticlePageCache();
    }

    /**
     * 博客文章列表（带 Redis 缓存）
     * 缓存策略：先读 Redis，未命中则查询数据库并回填缓存
     *
     * @param articlePageQueryDTO 查询参数
     * @return 分页列表
     */
    @Override
    public Page<ArticleListVO> pageBlogArticles(ArticlePageQueryDTO articlePageQueryDTO) {
        // 0.确保分页参数不为null，提供默认值
        int current = articlePageQueryDTO.getCurrent() != null ? articlePageQueryDTO.getCurrent() : 1;
        int size = articlePageQueryDTO.getSize() != null ? articlePageQueryDTO.getSize() : 10;

        // 1.构建缓存Key，尝试从 Redis 读取
        String cacheKey = buildArticlePageCacheKey(current, size,
                articlePageQueryDTO.getCategoryId(), articlePageQueryDTO.getTagId());
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("文章列表缓存命中: {}", cacheKey);
            return JSON.parseObject(cached, new TypeReference<Page<ArticleListVO>>() {
            });
        }

        // 2.缓存未命中，查询数据库
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        // 2.1 查询已发布的文章
        wrapper.eq(Article::getStatus, PUBLISHED);

        // 2.2 按分类筛选
        if (articlePageQueryDTO.getCategoryId() != null) {
            wrapper.eq(Article::getCategoryId, articlePageQueryDTO.getCategoryId());
        }

        // 2.3 按标签筛选
        if (articlePageQueryDTO.getTagId() != null) {
            List<Long> articleIds = articleTagMapper.selectList(
                    new LambdaQueryWrapper<ArticleTag>()
                            .eq(ArticleTag::getTagId, articlePageQueryDTO.getTagId()))
                    .stream()
                    .map(ArticleTag::getArticleId)
                    .toList();
            if (articleIds.isEmpty()) {
                return new Page<>(current, size, 0);
            }
            wrapper.in(Article::getId, articleIds);
        }

        // 2.4 先按置顶排序，再按发布时间排序
        wrapper.orderByDesc(Article::getIsTop).orderByDesc(Article::getPublishTime);

        // 3.分页查询
        Page<Article> page = page(new Page<>(current, size), wrapper);
        Page<ArticleListVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        // 3.1 将分页对象中的实体类转换为VO类，并且添加实时浏览量，填充分类名称，点赞数，标签名称
        voPage.setRecords(page.getRecords()
                .stream()
                .map(article -> {
                    ArticleListVO articleListVO = BeanUtil.copyProperties(article, ArticleListVO.class);
                    // 从Redis中获取文章的浏览量
                    articleListVO.setViewCount(getViewCountFromRedis(article.getId(), article.getViewCount()));
                    // 填充点赞数
                    articleListVO.setLikeCount(article.getLikeCount());
                    // 填充分类名称
                    if (article.getCategoryId() != null) {
                        Category category = categoryMapper.selectById(article.getCategoryId());
                        if (category != null) {
                            articleListVO.setCategoryName(category.getName());
                        }
                    }
                    // 填充标签名称
                    List<Long> tagIds = articleTagMapper.selectTagIdsByArticleId(article.getId());
                    if (!tagIds.isEmpty()) {
                        List<Tag> tags = tagMapper.selectBatchIds(tagIds);
                        articleListVO.setTags(tags.stream()
                                .map(this::toTagVO)
                                .toList());
                    } else {
                        articleListVO.setTags(Collections.emptyList());
                    }
                    return articleListVO;
                }).toList());

        // 4.回填Redis缓存
        try {
            redisTemplate.opsForValue().set(cacheKey,
                    JSON.toJSONString(voPage),
                    ARTICLE_PAGE_TTL_MINUTES, TimeUnit.MINUTES);
            log.debug("文章列表缓存已回填: {}", cacheKey);
        } catch (Exception e) {
            log.warn("文章列表缓存回填失败: {}", e.getMessage());
        }

        // 5.返回分页列表
        return voPage;
    }

    /**
     * 前台博客文章详情（带 Redis 缓存）
     * 缓存策略：先读 Redis 缓存，未命中则查询数据库并回填缓存
     * 注意：无论缓存是否命中，都会通过 Redis INCR 增加浏览量
     *
     * @param id 文章id
     * @return 前台文章详情
     */
    @Override
    public ArticleDetailVO getBlogArticleDetail(Long id) {
        // 1.使用Redis INCR 增加浏览量（无论缓存是否命中都要记录）
        redisTemplate.opsForValue().increment(ARTICLE_VIEW_KEY_PREFIX + id);

        // 2.尝试从 Redis 缓存中读取文章详情
        String cacheKey = ARTICLE_DETAIL_KEY_PREFIX + id;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("文章详情缓存命中: articleId={}", id);
            ArticleDetailVO cachedVO = JSON.parseObject(cached, ArticleDetailVO.class);
            // 缓存命中后，刷新实时数据（浏览量、点赞数）
            cachedVO.setViewCount(getViewCountFromRedis(id, cachedVO.getViewCount()));
            cachedVO.setLikeCount(getLikeCountFromRedis(id));
            return cachedVO;
        }

        // 3.缓存未命中，查询数据库
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException(ARTICLE_NOT_FOUND);
        }

        // 4.填充文章详情VO类
        ArticleDetailVO articleDetailVO = BeanUtil.copyProperties(article, ArticleDetailVO.class);
        // 4.1 设置实时浏览量
        articleDetailVO.setViewCount(getViewCountFromRedis(id, article.getViewCount()));
        // 4.2 填充点赞数
        articleDetailVO.setLikeCount(getLikeCountFromRedis(id));
        // 4.3 填充标签
        List<Long> tagIds = articleTagMapper.selectTagIdsByArticleId(id);
        if (!tagIds.isEmpty()) {
            List<Tag> tags = tagMapper.selectBatchIds(tagIds);
            articleDetailVO.setTags(tags.stream()
                    .map(this::toTagVO)
                    .toList());
        } else {
            articleDetailVO.setTags(Collections.emptyList());
        }
        // 4.4 填充分类名称
        if (article.getCategoryId() != null) {
            Category category = categoryMapper.selectById(article.getCategoryId());
            if (category != null) {
                articleDetailVO.setCategoryName(category.getName());
            }
        }
        // 4.5 填充作者名称
        if(article.getAuthorId() != null){
            SysUser user = sysUserMapper.selectById(article.getAuthorId());
            if(user != null){
                articleDetailVO.setAuthorNickname(user.getNickname());
            }
        }
        // 4.6 填充上一篇/下一篇（同为已发布状态）
        setPrevNextArticle(articleDetailVO, id);

        // 5.回填Redis缓存
        try {
            redisTemplate.opsForValue().set(cacheKey,
                    JSON.toJSONString(articleDetailVO),
                    ARTICLE_DETAIL_TTL_MINUTES, TimeUnit.MINUTES);
            log.debug("文章详情缓存已回填: articleId={}", id);
        } catch (Exception e) {
            log.warn("文章详情缓存回填失败: {}", e.getMessage());
        }

        // 6.返回文章详情VO类
        return articleDetailVO;
    }

    /**
     * 文章归档
     *
     * @return 文章归档列表（按照年月进行归档）
     */
    @Override
    public List<ArchiveVO> getBlogArticleArchive() {
        // 1.查询已发布的文章（数据库层面过滤掉空值）
        List<Article> articles = lambdaQuery()
                .select(Article::getId, Article::getTitle, Article::getCreateTime)
                .eq(Article::getStatus, PUBLISHED)
                .isNotNull(Article::getCreateTime)
                .orderByDesc(Article::getCreateTime)
                .list();
        if (articles.isEmpty()) {
            return Collections.emptyList();
        }

        // 2.创建归档列表
        List<ArchiveVO> result = new ArrayList<>();

        // 状态指针
        int lastYear = -1;
        int lastMonth = -1;
        ArchiveVO currentYearVO = null;
        ArchiveVO.ArchiveMonthVO currentMonthVO = null;

        // 3.遍历文章列表，设置归档列表，按照年、月进行归档
        for (Article article : articles) {
            LocalDateTime createTime = article.getCreateTime();
            int currentYear = createTime.getYear();
            int currentMonth = createTime.getMonthValue();

            // 年份变化 -> 创建新年份节点
            if (currentYear != lastYear) {
                currentYearVO = new ArchiveVO();
                currentYearVO.setYear(String.valueOf(currentYear));
                currentYearVO.setMonths(new ArrayList<>());
                result.add(currentYearVO);
                lastYear = currentYear;
                lastMonth = -1;// 跨年重置月份
            }

            // 月份变化 -> 创建新月份节点
            if (currentMonth != lastMonth) {
                currentMonthVO = new ArchiveVO.ArchiveMonthVO();
                currentMonthVO.setMonth(String.format(DAY_FORMAT_PATTERN, currentMonth));
                currentMonthVO.setCount(0);
                currentYearVO.getMonths().add(currentMonthVO);
                lastMonth = currentMonth;
            }

            // 组装文章节点
            ArchiveVO.ArchiveArticleVO archiveArticleVO = new ArchiveVO.ArchiveArticleVO();
            archiveArticleVO.setId(article.getId());
            archiveArticleVO.setTitle(article.getTitle());
            archiveArticleVO.setCreateTime(createTime.format(DateTimeFormatUtils.DATETIME_FORMATTER));
            archiveArticleVO.setDay(String.format(DAY_FORMAT_PATTERN, createTime.getDayOfMonth()));

            // 添加文章节点
            currentMonthVO.getArticles().add(archiveArticleVO);
            currentMonthVO.setCount(currentMonthVO.getCount() + 1);
        }

        // 4.返回归档列表
        return result;
    }

    /**
     * 文章点赞/取消点赞
     *
     * @param id 文章id
     * @param ip 用户IP
     * @return 最新点赞数
     */
    @Override
    @Transactional
    public Long likeArticle(Long id, String ip) {
        // 1.查询文章看是否存在和发布
        Article article = lambdaQuery()
                .select(Article::getId, Article::getStatus)
                .eq(Article::getId, id)
                .one();
        if (article == null || !article.getStatus().equals(PUBLISHED)) {
            throw new BusinessException(ARTICLE_NOT_FOUND);
        }

        // 2.检查是否已点赞（先查redis，再查数据库）
        String userKey = ARTICLE_USER_LIKE_KEY_PREFIX + id + ":" + ip;
        boolean hasLiked = Boolean.TRUE.equals(redisTemplate.hasKey(userKey));

        if (!hasLiked) {
            // 3.二次检查DB防止redis过期后的重复点赞
            Long count = articleLikeMapper.selectCount(new LambdaQueryWrapper<ArticleLike>()
                    .eq(ArticleLike::getArticleId, id)
                    .eq(ArticleLike::getIpAddress, ip));
            if (count > 0) {
                // 同步回Redis防止后续请求穿透到DB
                redisTemplate.opsForValue().set(userKey, "1", 24, TimeUnit.HOURS);
                hasLiked = true;
            }
        }

        Long newCount;
        if (hasLiked) {
            // 已点赞，执行取消点赞逻辑
            // 1.删除点赞记录
            articleLikeMapper.delete(new LambdaQueryWrapper<ArticleLike>()
                    .eq(ArticleLike::getArticleId, id)
                    .eq(ArticleLike::getIpAddress, ip));

            // 2.更新文章表点赞总数（DB-1）
            boolean success = update(new LambdaUpdateWrapper<Article>()
                    .setSql("like_count=like_count-1")
                    .eq(Article::getId, id)
                    .gt(Article::getLikeCount, 0)); // 确保点赞数不会小于0
            if (!success) {
                throw new BusinessException("取消点赞失败");
            }

            // 3.更新计数缓存（Redis-1）
            String countKey = ARTICLE_LIKE_COUNT_KEY_PREFIX + id;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(countKey))) {
                newCount = redisTemplate.opsForValue().decrement(countKey);
                if (newCount < 0) {
                    newCount = 0L;
                    redisTemplate.opsForValue().set(countKey, "0");
                }
            } else {
                // RedisKey不存在，回填并过期
                newCount = getLikeCountFromRedis(id);
            }

            // 4.删除Redis中的点赞标记
            redisTemplate.delete(userKey);
        } else {
            // 未点赞，执行点赞逻辑
            // 1.记录点赞流水（持久化）
            ArticleLike articleLike = ArticleLike.builder()
                    .articleId(id)
                    .ipAddress(ip)
                    .createTime(LocalDateTime.now())
                    .build();
            articleLikeMapper.insert(articleLike);

            // 2.更新文章表点赞总数（DB+1）
            boolean success = update(new LambdaUpdateWrapper<Article>()
                    .setSql("like_count=like_count+1")
                    .eq(Article::getId, id));
            if (!success) {
                throw new BusinessException("点赞失败");
            }

            // 3.更新计数缓存（Redis+1）
            String countKey = ARTICLE_LIKE_COUNT_KEY_PREFIX + id;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(countKey))) {
                newCount = redisTemplate.opsForValue().increment(countKey);
            } else {
                // RedisKey不存在，回填并过期
                newCount = getLikeCountFromRedis(id);
            }

            // 4.在Redis中标记已点赞
            redisTemplate.opsForValue().set(userKey, "1", 24, TimeUnit.HOURS);
        }

        // 5.返回最新点赞数
        return newCount;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 保存文章标签关联
     */

    private void saveArticleTags(Long articleId, List<Long> tagIds) {
        if (tagIds != null && !tagIds.isEmpty()) {
            // 1.创建文章标签关联
            LocalDateTime now = LocalDateTime.now();// 提前获取时间，保证所有记录时间一致，且避免在流中重复调用
            List<ArticleTag> articleTags = tagIds.stream().map(
                    tagId -> ArticleTag.builder()
                            .articleId(articleId)
                            .tagId(tagId)
                            .createTime(now)
                            .build())
                    .toList();
            // 2.保存文章标签关联
            articleTagMapper.batchInsertArticleTags(articleTags);
        }
    }

    /**
     * 从Redis中获取文章实时的浏览量
     * 其中Redis中储存的是增量计数，需要加上数据库中存储的浏览量
     *
     * @param ArticleId   文章id
     * @param dbViewCount 数据库的浏览量
     * @return 实时的浏览量
     */
    private Long getViewCountFromRedis(Long ArticleId, Long dbViewCount) {
        String viewKey = ARTICLE_VIEW_KEY_PREFIX + ArticleId;// 浏览量key
        String redisVal = redisTemplate.opsForValue().get(viewKey);// 从redis中获取浏览量
        long redisIncrement = (redisVal == null) ? 0 : Long.parseLong(redisVal);// redis中浏览量的增量
        long dbBase = (dbViewCount == null) ? 0 : dbViewCount;
        return redisIncrement + dbBase;
    }

    /**
     * 清除文章详情缓存
     */
    private void clearArticleDetailCache(Long articleId) {
        String articleDetailKey = ARTICLE_DETAIL_KEY_PREFIX + articleId;
        redisTemplate.delete(articleDetailKey);
    }

    /**
     * 清除分类/标签列表缓存
     */
    private void clearCategoryTagCache() {
        redisTemplate.delete(CATEGORY_LIST_KEY);
        redisTemplate.delete(TAG_LIST_KEY);
    }

    /**
     * 清除所有前台文章列表分页缓存
     * 使用 SCAN 命令通过前缀匹配删除，避免使用 KEYS 导致的性能问题
     */
    private void clearArticlePageCache() {
        try {
            Set<String> keys = redisTemplate.keys(ARTICLE_PAGE_KEY_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("文章列表缓存已清除，共删除 {} 个 Key", keys.size());
            }
        } catch (Exception e) {
            log.warn("清除文章列表缓存失败: {}", e.getMessage());
        }
    }

    /**
     * 构建前台文章列表分页缓存Key
     * 格式: blog:article:page:{current}:{size}:{categoryId}:{tagId}
     *
     * @param current    当前页码
     * @param size       每页条数
     * @param categoryId 分类ID
     * @param tagId      标签ID
     * @return 缓存Key
     */
    private String buildArticlePageCacheKey(int current, int size, Long categoryId, Long tagId) {
        return ARTICLE_PAGE_KEY_PREFIX + current + ":" + size
                + ":" + (categoryId == null ? "0" : categoryId)
                + ":" + (tagId == null ? "0" : tagId);
    }

    /**
     * 从 Redis 获取点赞数（优先读 Redis，未命中读 DB 并回填）
     */
    private Long getLikeCountFromRedis(Long articleId) {
        String key = ARTICLE_LIKE_COUNT_KEY_PREFIX + articleId;
        String val = redisTemplate.opsForValue().get(key);
        if (val != null) {
            return Long.parseLong(val);
        }

        // Redis 未命中，读 DB
        Article article = this.getById(articleId);
        Long likeCount = (article != null && article.getLikeCount() != null) ? article.getLikeCount() : 0L;

        // 回填 Redis (24小时过期)
        redisTemplate.opsForValue().set(key, String.valueOf(likeCount), 24, TimeUnit.HOURS);

        return likeCount;
    }

    /**
     * 设置上一篇和下一篇
     *
     * @param vo        文章详情VO
     * @param CurrentId 当前文章ID
     */
    private void setPrevNextArticle(ArticleDetailVO vo, Long CurrentId) {
        // 上一篇：ID小于当前，按ID降序取第一条
        Article prev = getOne(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getStatus, PUBLISHED) // 已发布
                        .lt(Article::getId, CurrentId)// 小于当前ID
                        .select(Article::getId, Article::getTitle)// 只查询ID和标题
                        .orderByDesc(Article::getId)// 按ID降序
                        .last("LIMIT 1"));
        if (prev != null) {
            ArticleDetailVO.ArticleNavVO prevNav = new ArticleDetailVO.ArticleNavVO();// 上一篇
            prevNav.setId(prev.getId());// ID
            prevNav.setTitle(prev.getTitle());// 标题
            vo.setPrevArticle(prevNav);// 设置上一篇
        }

        // 下一篇：ID大于当前，按ID升序取第一条
        Article next = getOne(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getStatus, PUBLISHED) // 已发布
                        .gt(Article::getId, CurrentId)// 大于当前ID
                        .select(Article::getId, Article::getTitle)// 只查询ID和标题
                        .orderByAsc(Article::getId)// 按ID升序
                        .last("LIMIT 1"));
        if (next != null) {
            ArticleDetailVO.ArticleNavVO nextNav = new ArticleDetailVO.ArticleNavVO();// 下一篇
            nextNav.setId(next.getId());// ID
            nextNav.setTitle(next.getTitle());// 标题
            vo.setNextArticle(nextNav);// 设置下一篇
        }
    }

    /**
     * Tag PO -> TagVO 转换
     */
    private TagVO toTagVO(Tag tag) {
        return BeanUtil.copyProperties(tag, TagVO.class);
    }

    /**
     * 根据标题生成slug
     * 
     * @param title 文章标题
     * @return 生成的slug
     */
    private String generateSlugFromTitle(String title) {
        if (StrUtil.isBlank(title)) {
            return "";
        }

        // 转小写
        String slug = title.toLowerCase();

        // 替换空格为横线
        slug = slug.replaceAll("\\s+", "-");

        // 移除非字母数字和横线的字符
        slug = slug.replaceAll("[^a-z0-9-]", "");

        // 去除首尾横线
        slug = slug.trim().replaceAll("^-+|-+$", "");

        // 限制长度
        if (slug.length() > 100) {
            slug = slug.substring(0, 100);
        }

        return slug;
    }

    /**
     * 确保slug的唯一性
     * 
     * @param slug 原始slug
     * @return 唯一的slug
     */
    private String ensureUniqueSlug(String slug) {
        if (StrUtil.isBlank(slug)) {
            return slug;
        }

        String originalSlug = slug;
        int suffix = 1;

        // 检查slug是否已存在
        while (true) {
            LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Article::getSlug, slug);
            long count = count(wrapper);

            if (count == 0) {
                // slug不存在，可以使用
                break;
            }

            // slug已存在，添加后缀
            slug = originalSlug + "-" + suffix;
            suffix++;
        }

        return slug;
    }
}
