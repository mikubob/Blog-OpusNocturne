package com.xuan.service.controller.blog;

import com.xuan.common.domain.Result;
import com.xuan.entity.dto.friendLink.FriendLinkApplyAndUpdateDTO;
import com.xuan.entity.po.interact.FriendLink;
import com.xuan.service.service.IFriendLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前台友情链接控制器
 *
 * @author 玄〤
 */
@Tag(name = "前台友情链接")
@RestController("blogFriendLinkController")
@RequestMapping("/api/blog/friend-link")
@RequiredArgsConstructor
public class FriendLinkController {

    private final IFriendLinkService friendLinkService;

    @Operation(summary = "申请友情链接")
    @PostMapping
    public Result<Void> applyFriendLink(@Validated @RequestBody FriendLinkApplyAndUpdateDTO dto) {
        friendLinkService.applyFriendLink(dto);
        return Result.success(null, "申请已提交，请等待管理员审核");
    }

    @Operation(summary = "获取现有友链列表")
    @GetMapping("/list")
    public Result<List<FriendLink>> listFriendLinks() {
        return Result.success(friendLinkService.listPublicFriendLinks());
    }
}
