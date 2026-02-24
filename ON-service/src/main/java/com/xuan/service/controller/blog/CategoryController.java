package com.xuan.service.controller.blog;

import com.xuan.common.domain.Result;
import com.xuan.entity.vo.category.CategoryVO;
import com.xuan.service.service.ICategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前台分类控制器
 */
@Tag(name = "前台分类")
@RestController("blogCategoryController")
@RequestMapping("/api/blog/category")
@RequiredArgsConstructor
public class CategoryController {

    private final ICategoryService categoryService;

    @Operation(summary = "获取所有分类")
    @GetMapping("/list")
    public Result<List<CategoryVO>> listCategories() {
        return Result.success(categoryService.listAllCategories());
    }
}