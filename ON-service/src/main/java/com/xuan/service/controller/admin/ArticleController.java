package com.xuan.service.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuan.common.domain.Result;
import com.xuan.entity.dto.article.ArticleAdminPageQueryDTO;
import com.xuan.entity.dto.article.ArticleCreateDTO;
import com.xuan.entity.dto.article.ArticleStatusDTO;
import com.xuan.entity.dto.article.ArticleTopDTO;
import com.xuan.entity.dto.article.ArticleUpdateDTO;
import com.xuan.entity.vo.article.ArticleAdminDetailVO;
import com.xuan.entity.vo.article.ArticleAdminListVO;
import com.xuan.entity.vo.article.ArticleCreatVO;
import com.xuan.service.service.IArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name="后台文章管理相关接口")
@RestController
@RequestMapping("/api/admin/article")
@RequiredArgsConstructor
public class ArticleController {

    private final IArticleService articleService;

    @Operation(summary = "创建文章")
    @PostMapping
    public Result<ArticleCreatVO> creatArticle(@Validated @RequestBody ArticleCreateDTO articleCreateDTO){
        return Result.success(articleService.createArticle(articleCreateDTO));
    }

    @Operation(summary = "后台文章详情")
    @GetMapping("/{id}")
    public Result<ArticleAdminDetailVO> getArticleDetail(@PathVariable("id") Long id){
        return Result.success(articleService.getArticleDetail(id));
    }

    @Operation(summary = "后台文章列表")
    @GetMapping("/page")
    public Result<Page<ArticleAdminListVO>> pageAdminArticles(@Validated ArticleAdminPageQueryDTO articleAdminPageQueryDTO){
        return Result.success(articleService.pageAdminArticles(articleAdminPageQueryDTO));
    }

    @Operation(summary = "更新文章")
    @PutMapping("/{id}")
    public Result<Void> updateArticle(@PathVariable Long id, @Validated @RequestBody ArticleUpdateDTO articleUpdateDTO){
        articleService.updateArticle(id,articleUpdateDTO);
        return Result.success();
    }

    @Operation(summary = "删除文章")
    @DeleteMapping("/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id){
        articleService.deleteArticle(id);
        return Result.success();
    }

    @Operation(summary = "批量删除文章")
    @DeleteMapping("/batch-delete")
    public Result<Void> batchDeleteArticle(@RequestBody Long[] ids){
        articleService.batchDeleteArticle(ids);
        return Result.success();
    }

    @Operation(summary = "文章置顶/取消置顶")
    @PutMapping("/{id}/top")
    public Result<Void> updateArticleTop(@PathVariable Long id ,@RequestBody ArticleTopDTO articleTopDTO){
        articleService.updateArticleTop(id,articleTopDTO);
        return Result.success();
    }

    @Operation(summary = "更新文章状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateArticleStatus(@PathVariable Long id , @RequestBody ArticleStatusDTO articleStatusDTO){
        articleService.updateArticleStatus(id,articleStatusDTO);
        return Result.success();
    }
}
