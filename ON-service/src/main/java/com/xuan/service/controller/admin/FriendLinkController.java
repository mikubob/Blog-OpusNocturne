package com.xuan.service.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuan.common.domain.Result;
import com.xuan.entity.dto.friendLink.FriendLinkApplyAndUpdateDTO;
import com.xuan.entity.dto.friendLink.FriendLinkAuditDTO;
import com.xuan.entity.dto.friendLink.FriendLinkPageQueryDTO;
import com.xuan.entity.po.interact.FriendLink;
import com.xuan.service.service.IFriendLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 后台友情链接管理控制器
 *
 * @author 玄〤
 */
@Tag(name = "后台友情链接管理")
@RestController("adminFriendLinkController")
@RequestMapping("/api/admin/friend-link")
@RequiredArgsConstructor
public class FriendLinkController {

    private final IFriendLinkService friendLinkService;

    @Operation(summary = "分页获取友链列表")
    @GetMapping("/page")
    public Result<Page<FriendLink>> pageFriendLinks(@Validated FriendLinkPageQueryDTO query) {
        return Result.success(friendLinkService.pageFriendLinks(query));
    }

    @Operation(summary = "审核友链")
    @PutMapping("/{id}/audit")
    public Result<Void> auditFriendLink(@PathVariable Long id, @Validated @RequestBody FriendLinkAuditDTO dto) {
        friendLinkService.auditFriendLink(id, dto);
        return Result.success();
    }

    @Operation(summary = "修改友链")
    @PutMapping("/{id}")
    public Result<Void> updateFriendLink(@PathVariable Long id, @Validated @RequestBody FriendLinkApplyAndUpdateDTO dto) {
        FriendLink friendLink = BeanUtil.copyProperties(dto, FriendLink.class);
        friendLink.setId(id);
        friendLinkService.updateById(friendLink);
        return Result.success();
    }

    @Operation(summary = "删除友链")
    @DeleteMapping("/{id}")
    public Result<Void> deleteFriendLink(@PathVariable Long id) {
        friendLinkService.removeById(id);
        return Result.success();
    }
}
