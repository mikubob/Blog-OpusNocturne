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
import java.util.stream.Collectors;

import static com.xuan.common.enums.ArticleStatusEnum.PUBLISHED;
import static com.xuan.common.enums.ErrorCode.ARTICLE_CREATE_FAILED;
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
    public void updateArticle(Long id, ArticleUpdateDTO articleUpdateDTO) {

    }

    /**
     * 删除文章
     *
     * @param id 文章id
     */
    @Override
    public void deleteArticle(Long id) {

    }

    /**
     * 批量删除文章
     *
     * @param ids 文章id数组
     */
    @Override
    public void batchDeleteArticle(Long[] ids) {

    }

    /**
     * 文章置顶/取消置顶
     *
     * @param id            文章id
     * @param articleTopDTO 置顶参数
     */
    @Override
    public void updateArticleTop(Long id, ArticleTopDTO articleTopDTO) {

    }

    /**
     * 更新文章状态
     *
     * @param id               文章id
     * @param articleStatusDTO 状态参数
     */
    @Override
    public void updateArticleStatus(Long id, ArticleStatusDTO articleStatusDTO) {

    }

    /**
     * 从Redis中获取文章实时的浏览量
     * 其中Redis中储存的是增量计数，需要加上数据库中存储的浏览量
     * @param ArticleId 文章id
     * @param dbViewCount 数据库的浏览量
     * @return 实时的浏览量
     */
    private Long getViewCountFromRedis(Long ArticleId, Long dbViewCount){
        String viewKey= RedisConstant.ARTICLE_VIEW_KEY_PREFIX+ArticleId;//浏览量key
        String redisVal=redisTemplate.opsForValue().get(viewKey);//从redis中获取浏览量
        long redisIncrement=(redisVal==null)?0:Long.parseLong(redisVal);//redis中浏览量的增量
        long dbBase=(dbViewCount==null)?0:dbViewCount;
        return redisIncrement+dbBase;
    }
}
