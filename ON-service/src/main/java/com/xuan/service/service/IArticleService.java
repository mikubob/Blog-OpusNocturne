package com.xuan.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xuan.entity.dto.article.ArticleAdminPageQueryDTO;
import com.xuan.entity.dto.article.ArticleCreateDTO;
import com.xuan.entity.dto.article.ArticlePageQueryDTO;
import com.xuan.entity.dto.article.ArticleStatusDTO;
import com.xuan.entity.dto.article.ArticleTopDTO;
import com.xuan.entity.dto.article.ArticleUpdateDTO;
import com.xuan.entity.po.blog.Article;
import com.xuan.entity.vo.article.ArchiveVO;
import com.xuan.entity.vo.article.ArticleAdminDetailVO;
import com.xuan.entity.vo.article.ArticleAdminListVO;
import com.xuan.entity.vo.article.ArticleCreatVO;
import com.xuan.entity.vo.article.ArticleDetailVO;
import com.xuan.entity.vo.article.ArticleListVO;

import java.util.List;

public interface IArticleService extends IService<Article> {

    /**
     * 创建文章
     * @param articleCreateDTO 文章创建DTO
     * @return 文章的id和标题
     */
    ArticleCreatVO createArticle(ArticleCreateDTO articleCreateDTO);

    /**
     * 后台文章详情
     * @param id 文章id
     * @return 文章详情
     */
    ArticleAdminDetailVO getArticleDetail(Long id);

    /**
     * 后台文章列表
     * @param articleAdminPageQueryDTO 查询参数
     * @return 文章列表
     */
    Page<ArticleAdminListVO> pageAdminArticles(ArticleAdminPageQueryDTO articleAdminPageQueryDTO);

    /**
     * 更新文章
     * @param id 文章id
     * @param articleUpdateDTO 更新参数
     */
    void updateArticle(Long id, ArticleUpdateDTO articleUpdateDTO);

    /**
     * 删除文章
     * @param id 文章id
     */
    void deleteArticle(Long id);

    /**
     * 批量删除文章
     * @param ids 文章id集合
     */
    void batchDeleteArticle(List<Long> ids);

    /**
     * 文章置顶/取消置顶
     * @param id 文章id
     * @param articleTopDTO 置顶参数
     */
    void updateArticleTop(Long id, ArticleTopDTO articleTopDTO);

    /**
     * 更新文章状态
     * @param id 文章id
     * @param articleStatusDTO 状态参数
     */
    void updateArticleStatus(Long id, ArticleStatusDTO articleStatusDTO);

    /**
     * 前台文章列表
     * @param articlePageQueryDTO 查询参数
     * @return 文章列表
     */
    Page<ArticleListVO> pageBlogArticles(ArticlePageQueryDTO articlePageQueryDTO);

    /**
     * 前台文章详情
     * @param id 文章id
     * @return 文章详情
     */
    ArticleDetailVO getBlogArticleDetail(Long id);

    /**
     * 文章归档
     * @return 文章归档
     */
    List<ArchiveVO> getBlogArticleArchive();

    /**
     * 文章点赞
     * @param id 文章id
     * @return 点赞数量
     */
    Long likeArticle(Long id, String ip);
}
