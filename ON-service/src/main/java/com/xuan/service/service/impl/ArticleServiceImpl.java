package com.xuan.service.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuan.common.constant.RedisConstant;
import com.xuan.common.exceptions.BusinessException;
import com.xuan.entity.dto.article.ArticleAdminPageQueryDTO;
import com.xuan.entity.dto.article.ArticleCreateDTO;
import com.xuan.entity.dto.article.ArticleStatusDTO;
import com.xuan.entity.dto.article.ArticleTopDTO;
import com.xuan.entity.dto.article.ArticleUpdateDTO;
import com.xuan.entity.po.blog.Article;
import com.xuan.entity.vo.article.ArticleAdminDetailVO;
import com.xuan.entity.vo.article.ArticleAdminListVO;
import com.xuan.entity.vo.article.ArticleCreatVO;
import com.xuan.service.mapper.ArticleMapper;
import com.xuan.service.service.IArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.xuan.common.constant.RedisConstant.ARTICLE_DETAIL_KEY_PREFIX;
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

        //TODO 2.处理标签关联
        //TODO 3.清除分类/标签缓存（文章数量发生变化）

        //4.返回文章创建VO
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
        //1.查询文章,并做非空判断
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException(ARTICLE_NOT_FOUND);
        }

        //2.转换为VO
        ArticleAdminDetailVO articleAdminDetailVO = BeanUtil.copyProperties(article, ArticleAdminDetailVO.class);

        //TODO3.填充分类名称
        //TODO4.填充标签信息

        //5.返回文章详情VO
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
        //1. 根据标题模糊查询
        if (articleAdminPageQueryDTO.getTitle() != null && !articleAdminPageQueryDTO.getTitle().isEmpty()) {
            wrapper.like(Article::getTitle, articleAdminPageQueryDTO.getTitle());
        }
        //2. 根据分类id查询
        if (articleAdminPageQueryDTO.getCategoryId() != null) {
            wrapper.eq(Article::getCategoryId, articleAdminPageQueryDTO.getCategoryId());
        }
        //3. 根据状态查询
        if (articleAdminPageQueryDTO.getStatus() != null) {
            wrapper.eq(Article::getStatus, articleAdminPageQueryDTO.getStatus());
        }
        //4. 根据创建时间倒序排序
        wrapper.orderByDesc(Article::getCreateTime);
        //5. 分页查询
        //5.1确保分页参数不为null，提供默认值
        int current = articleAdminPageQueryDTO.getCurrent() != null ? articleAdminPageQueryDTO.getCurrent() : 1;
        int size = articleAdminPageQueryDTO.getSize() != null ? articleAdminPageQueryDTO.getSize() : 10;
        //5.2 创建分页对象
        Page<Article> page = page(new Page<>(current, size), wrapper);
        Page<ArticleAdminListVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        //5.3 将分页对象中的实体类转换为VO类，并且添加实时浏览量，分类名称，作者昵称
        voPage.setRecords(page.getRecords()//获取原始数据
                .stream()
                .map(article -> {
                    ArticleAdminListVO articleAdminListVO = BeanUtil.copyProperties(article, ArticleAdminListVO.class);//转换为VO
                    // 从Redis中获取实时浏览量
                    articleAdminListVO.setViewCount(getViewCountFromRedis(article.getId(),article.getViewCount()));
                    //TODO填充分类名称
                    //TODO填充作者昵称
                    return articleAdminListVO;
                }).collect(Collectors.toList())
        );
        //6. 返回分页列表
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
        //1.查询文章并做非空判断
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException(ARTICLE_NOT_FOUND);
        }
        //2.更新文章基本信息
        BeanUtil.copyProperties(articleUpdateDTO, article,"id");
        if (article.getStatus().equals(PUBLISHED)&&article.getPublishTime() == null){
            article.setPublishTime(LocalDateTime.now());
        }
        updateById(article);

        //TODO 3.更新标签关联：先删除旧的，再插入新的
        //4.清除文章详情缓存和分类/标签列表缓存
        clearArticleDetailCache(id);
        clearCategoryTagCache();

    }

    /**
     * 删除文章
     *
     * @param id 文章id
     */
    @Override
    @Transactional
    public void deleteArticle(Long id) {
        //1.查询文章并做非空判断
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException(ARTICLE_NOT_FOUND);
        }
        //2.删除文章
        removeById(id);
        //TODO 3.删除标签关联

        //4.清除文章详情缓存和分类/标签列表缓存
        clearArticleDetailCache(id);
        clearCategoryTagCache();

        //5.删除文章实时浏览量缓存
        redisTemplate.delete(ARTICLE_VIEW_KEY_PREFIX + id);
    }

    /**
     * 批量删除文章
     *
     * @param ids 文章id集合
     */
    @Override
    @Transactional
    public void batchDeleteArticle(List<Long>ids) {
        //1.判断ids是否为空
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ARTICLE_DELETE_EMPTY);
        }

        //2.检查文章是否存在
        List<Article> articles = listByIds(ids);
        List<Long> existingIds = articles.stream()
                .map(Article::getId)
                .toList();
        if (existingIds.isEmpty()){
            throw new BusinessException(ARTICLE_NOT_FOUND);
        }

        //3.找出不存在的文章id
        List<Long> notExistingIds = ids.stream()
                .filter(id->!existingIds.contains(id))
                .toList();
        if (!notExistingIds.isEmpty()){
            throw new BusinessException("文章【"+notExistingIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","))+"】不存在或者已被删除");
        }

        //TODO4.批量删除文章及其关联的标签
        removeBatchByIds(ids);
        for (Long id : ids) {
            //TODO articleTagMapper.deleteByArticleId(id);

            //清除文章详情缓存
            clearArticleDetailCache(id);
            redisTemplate.delete(ARTICLE_VIEW_KEY_PREFIX + id);
        }

        // 5.清除分类/标签列表缓存
        clearCategoryTagCache();
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
        //1.查询文章并做非空判断
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException(ARTICLE_NOT_FOUND);
        }

        //2.更新文章置顶状态
        article.setIsTop(articleTopDTO.getIsTop());
        updateById(article);
    }

    /**
     * 更新文章状态
     *
     * @param id               文章id
     * @param articleStatusDTO 状态参数
     */
    @Override
    public void updateArticleStatus(Long id, ArticleStatusDTO articleStatusDTO) {
        //1.查询文章并做非空判断
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException(ARTICLE_NOT_FOUND);
        }
        //2.更新文章状态
        article.setStatus(articleStatusDTO.getStatus());
        //如果文章状态为发布，则设置发布时间
        if (article.getStatus().equals(PUBLISHED)&&article.getPublishTime() == null){
            article.setPublishTime(LocalDateTime.now());
        }
        updateById(article);
        //3.清除文章详情缓存
        clearArticleDetailCache(id);
    }

    /**
     * 从Redis中获取文章实时的浏览量
     * 其中Redis中储存的是增量计数，需要加上数据库中存储的浏览量
     * @param ArticleId 文章id
     * @param dbViewCount 数据库的浏览量
     * @return 实时的浏览量
     */
    private Long getViewCountFromRedis(Long ArticleId, Long dbViewCount){
        String viewKey= ARTICLE_VIEW_KEY_PREFIX+ArticleId;//浏览量key
        String redisVal=redisTemplate.opsForValue().get(viewKey);//从redis中获取浏览量
        long redisIncrement=(redisVal==null)?0:Long.parseLong(redisVal);//redis中浏览量的增量
        long dbBase=(dbViewCount==null)?0:dbViewCount;
        return redisIncrement+dbBase;
    }

    /**
     * 清除文章详情缓存
     */
    private void clearArticleDetailCache(Long articleId){
        String articleDetailKey=ARTICLE_DETAIL_KEY_PREFIX+articleId;
        redisTemplate.delete(articleDetailKey);
    }

    /**
     * 清除分类/标签列表缓存
     */
    private void clearCategoryTagCache() {
        redisTemplate.delete(CATEGORY_LIST_KEY);
        redisTemplate.delete(TAG_LIST_KEY);
    }
}
