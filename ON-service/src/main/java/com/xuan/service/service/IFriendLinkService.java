package com.xuan.service.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xuan.entity.dto.friendLink.FriendLinkAuditDTO;
import com.xuan.entity.dto.friendLink.FriendLinkPageQueryDTO;
import com.xuan.entity.po.interact.FriendLink;

import java.util.List;

/**
 * 友情链接服务接口
 *
 * @author 玄〤
 */
public interface IFriendLinkService extends IService<FriendLink> {
    /**
     * 分页查询友情链接
     * @param query 查询参数
     * @return 分页结果
     */
    Page<FriendLink> pageFriendLinks(FriendLinkPageQueryDTO query);


    /**
     * 审核友情链接
     * @param id 友情链接ID
     * @param dto 审核参数
     */
    void auditFriendLink(Long id, FriendLinkAuditDTO dto);
}
