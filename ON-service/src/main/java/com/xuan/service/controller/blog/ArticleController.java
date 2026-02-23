package com.xuan.service.controller.blog;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuan.common.domain.Result;
import com.xuan.common.utils.IpUtils;
import com.xuan.entity.dto.article.ArticlePageQueryDTO;
import com.xuan.entity.vo.article.ArchiveVO;
import com.xuan.entity.vo.article.ArticleDetailVO;
import com.xuan.entity.vo.article.ArticleListVO;
import com.xuan.service.service.IArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台文章控制器
 */
@Tag(name = "前台文章")
@RestController("blogArticleController")
@RequestMapping("/api/blog/article")
@RequiredArgsConstructor
public class ArticleController {

    private final IArticleService articleService;

    @Operation(summary = "前台文章列表")
    @GetMapping("/page")
    public Result<Page<ArticleListVO>> pageBlogArticles(@Validated ArticlePageQueryDTO articlePageQueryDTO){
        return Result.success(articleService.pageBlogArticles(articlePageQueryDTO));
    }

    @Operation(summary = "前台文章详情")
    @GetMapping("/{id}")
    public Result<ArticleDetailVO> getBlogArticleDetail(@PathVariable Long id){
        return Result.success(articleService.getBlogArticleDetail(id));
    }

    @Operation(summary = "文章归档")
    @GetMapping("/archive")
    public Result<List<ArchiveVO>> getBlogArticleArchive(){
        return Result.success(articleService.getBlogArticleArchive());
    }

    @Operation(summary = "文章点赞")
    @GetMapping("/{id}/like")
    public Result<Long> likeArticle(@PathVariable Long id, HttpServletRequest request){
        String ip= IpUtils.getIpAddr(request);
        return Result.success(articleService.likeArticle(id,ip));
    }

}
