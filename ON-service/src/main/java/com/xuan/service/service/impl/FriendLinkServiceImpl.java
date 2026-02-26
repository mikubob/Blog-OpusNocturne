package com.xuan.service.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuan.common.exceptions.BusinessException;
import com.xuan.entity.dto.friendLink.FriendLinkApplyAndUpdateDTO;
import com.xuan.entity.dto.friendLink.FriendLinkAuditDTO;
import com.xuan.entity.dto.friendLink.FriendLinkPageQueryDTO;
import com.xuan.entity.po.interact.FriendLink;
import com.xuan.service.mapper.FriendLinkMapper;
import com.xuan.service.service.IFriendLinkService;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.xuan.common.enums.ErrorCode.FRIEND_LINK_ALREADY_APPLIED;
import static com.xuan.common.enums.ErrorCode.FRIEND_LINK_NOT_FOUND;
import static com.xuan.common.enums.FriendLinkStatusEnum.ONLINE;
import static com.xuan.common.enums.FriendLinkStatusEnum.PENDING;

/**
 * 友情链接服务实现类
 *
 * @author 玄〤
 */
@Service
public class FriendLinkServiceImpl extends ServiceImpl<FriendLinkMapper, FriendLink> implements IFriendLinkService {

    /**
     * 分页查询友情链接
     * @param query 查询参数
     * @return 分页结果
     */
    @Override
    public Page<FriendLink> pageFriendLinks(FriendLinkPageQueryDTO query) {
        //1.确保分页参数不为null，提供默认值
        int currentPage = query.getCurrent() != null ? query.getCurrent() : 1;
        int pageSize = query.getSize() != null ? query.getSize() : 10;
        //2.创建分页对象
        Page<FriendLink> page = new Page<>(currentPage, pageSize);
        //3.设置查询分页条件
        LambdaQueryWrapper<FriendLink> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getStatus()!=null,FriendLink::getStatus,query.getStatus())
                .like(StrUtil.isNotBlank(query.getName()),FriendLink::getName,query.getName())
                .orderByDesc(FriendLink::getCreateTime);
        //4.返回结果
        return page(page,wrapper);
    }

    /**
     * 审核友情链接
     * @param id 友情链接ID
     * @param dto 审核参数
     */
    @Override
    public void auditFriendLink(Long id, FriendLinkAuditDTO dto) {
        //1.查询友链看是否存在
        FriendLink friendLink = getById(id);
        if (friendLink==null){
            throw new BusinessException(FRIEND_LINK_NOT_FOUND);
        }
        //2.设置审核结果
        friendLink.setStatus(dto.getStatus());
        //3.保存
        updateById(friendLink);
        // TODO: 如果审核未通过，可发送邮件通知站长 (需集成邮件服务)
    }

    /**
     * 申请友情链接
     * @param dto 申请参数
     */
    @Override
    public void applyFriendLink(FriendLinkApplyAndUpdateDTO dto) {
        // 1.校验申请友链是否已经存在（根据URL）
        long count = count(new LambdaQueryWrapper<FriendLink>()
                .eq(FriendLink::getUrl, dto.getUrl()));
        if (count>0){
            throw new BusinessException(FRIEND_LINK_ALREADY_APPLIED);
        }
        //2.设置友链中参数
        FriendLink friendLink = BeanUtil.copyProperties(dto, FriendLink.class);
        friendLink.setStatus(PENDING);
        friendLink.setSort(0);
        save(friendLink);
    }

    /**
     * 获取公开友情链接
     * @return 公开友情链接
     */
    @Override
    public List<FriendLink> listPublicFriendLinks() {
        // 仅查询状态为 1 (上线) 的友链，按排序降序、创建时间升序排列
        return list(new LambdaQueryWrapper<FriendLink>()
                .eq(FriendLink::getStatus, ONLINE.getCode())
                .orderByDesc(FriendLink::getSort)
                .orderByAsc(FriendLink::getCreateTime));
    }
}
