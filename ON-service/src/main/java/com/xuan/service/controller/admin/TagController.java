package com.xuan.service.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuan.common.domain.Result;
import com.xuan.entity.dto.tag.TagDTO;
import com.xuan.entity.dto.tag.TagPageQueryDTO;
import com.xuan.entity.vo.tag.TagAdminVO;
import com.xuan.service.service.ITagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台标签管理控制器
 */
@Tag(name = "后台标签管理")
@RestController
@RequestMapping("/api/admin/tag")
@RequiredArgsConstructor
public class TagController {

    private final ITagService tagService;

    @Operation(summary = "分页获取标签列表")
    @GetMapping("/list")
    public Result<Page<TagAdminVO>> pageTags(@Validated TagPageQueryDTO queryDTO) {
        return Result.success(tagService.pageTags(queryDTO));
    }

    @Operation(summary = "创建标签")
    @PostMapping
    public Result<Void> createTag(@Validated @RequestBody TagDTO tagDTO) {
        tagService.createTag(tagDTO);
        return Result.success();
    }

    @Operation(summary = "更新标签")
    @PutMapping("/{id}")
    public Result<Void> updateTag(@PathVariable Long id, @Validated @RequestBody TagDTO tagDTO) {
        tagService.updateTag(id, tagDTO);
        return Result.success();
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return Result.success();
    }

    @Operation(summary = "批量删除标签")
    @DeleteMapping("/batch-delete")
    public Result<Void> batchDeleteTags(@RequestBody List<Long> ids) {
        tagService.batchDeleteTags(ids);
        return Result.success();
    }
}