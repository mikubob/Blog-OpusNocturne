package com.xuan.service.controller.blog;

import com.xuan.common.domain.Result;
import com.xuan.entity.vo.tag.TagVO;
import com.xuan.service.service.ITagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前台标签控制器
 */
@Tag(name = "前台标签")
@RestController("blogTagController")
@RequestMapping("/api/blog/tag")
@RequiredArgsConstructor
public class TagController {

    private final ITagService tagService;

    @Operation(summary = "获取所有标签")
    @GetMapping("/list")
    public Result<List<TagVO>> listTags() {
        return Result.success(tagService.listAllTags());
    }
}