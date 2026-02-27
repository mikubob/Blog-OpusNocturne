package com.xuan.service.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuan.common.exceptions.BusinessException;
import com.xuan.entity.dto.comment.CommentAuditDTO;
import com.xuan.entity.dto.comment.CommentCreateDTO;
import com.xuan.entity.dto.comment.CommentPageQueryDTO;
import com.xuan.entity.po.blog.Article;
import com.xuan.entity.po.interact.Comment;
import com.xuan.entity.vo.comment.CommentAdminVO;
import com.xuan.entity.vo.comment.CommentPageVO;
import com.xuan.entity.vo.comment.CommentTreeVO;
import com.xuan.entity.vo.system.SystemSettingVO;
import com.xuan.service.mapper.ArticleMapper;
import com.xuan.service.mapper.CommentMapper;
import com.xuan.service.service.ICommentService;
import com.xuan.service.service.ISysSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.xuan.common.enums.CommentStatusEnum.APPROVED;
import static com.xuan.common.enums.CommentStatusEnum.PENDING;
import static com.xuan.common.enums.ErrorCode.COMMENT_AUDIT_EMPTY;
import static com.xuan.common.enums.ErrorCode.COMMENT_DELETE_EMPTY;
import static com.xuan.common.enums.ErrorCode.COMMENT_NOT_FOUND;

/**
 * 评论服务实现类
 *
 * @author 玄〤
 * @since 2026-02-20
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    private final ArticleMapper articleMapper;
    private final ISysSettingService SysSettingService;

    /**
     * 前台：分页获取文章评论树
     * <p>
     * 【两级分页策略，解决大数据量 OOM 问题】
     * 
     * <pre>
     * Step1 - 外层分页：仅查出当前页的顶级评论（rootParentId IS NULL），数量可控。
     * Step2 - 内层批量查：用 IN(rootIds) 一次查出这些顶级评论下的全部子评论。
     *         子评论数量受每页顶级评论数量约束，不会无限膨胀。
     * Step3 - 内存组装：将子评论挂载到对应顶级节点上，返回树形数据。
     * </pre>
     *
     * @param articleId 文章ID
     * @param current   当前页码（从 1 开始）
     * @param size      每页顶级评论条数
     * @return 分页评论树（含顶级评论总数 + 当前页树形列表）
     */
    @Override
    public CommentPageVO getCommentTree(Long articleId, int current, int size) {
        // ==================== Step1: 外层分页 - 只查顶级评论 ====================
        // 顶级评论的特征：rootParentId IS NULL（未挂载到任何根评论下）
        Page<Comment> rootPage = page(
                new Page<>(current, size),
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getArticleId, articleId)
                        .eq(Comment::getStatus, APPROVED)
                        .isNull(Comment::getRootParentId)
                        .orderByAsc(Comment::getCreateTime));

        List<Comment> rootComments = rootPage.getRecords();

        // 短路返回：当前页没有顶级评论，无需继续查子评论
        if (rootComments.isEmpty()) {
            return new CommentPageVO(rootPage.getTotal(), new ArrayList<>());
        }

        // ==================== Step2: 内层批量查 - IN(rootIds) ====================
        // 收集本页所有顶级评论的 ID
        List<Long> rootIds = rootComments.stream().map(Comment::getId).toList();

        // 利用数据库联合索引 idx_article_root(article_id, root_parent_id) 高效查询
        List<Comment> childComments = list(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getArticleId, articleId)
                        .eq(Comment::getStatus, APPROVED)
                        .in(Comment::getRootParentId, rootIds)
                        .orderByAsc(Comment::getCreateTime));

        // ==================== Step3: 内存组装 ====================
        List<CommentTreeVO> tree = buildCommentTree(rootComments, childComments);
        return new CommentPageVO(rootPage.getTotal(), tree);
    }

    /**
     * 获取文章评论统计
     * @param articleId 文章ID
     * @return 评论统计
     */
    @Override
    public Map<String, Long> getArticleCommentStats(Long articleId) {
        //1.获取评论总数
        Long total = lambdaQuery()
                .eq(Comment::getArticleId, articleId)
                .eq(Comment::getStatus, APPROVED)
                .count();
        //2.获取一级评论数
        Long rootCount = lambdaQuery()
                .eq(Comment::getArticleId, articleId)
                .eq(Comment::getStatus, APPROVED)
                .isNull(Comment::getRootParentId)
                .count();

        //3.构建返回结果
        Map<String, Long> result = new HashMap<>();
        result.put("total", total!=null?total:0L);
        result.put("rootCount", rootCount!=null?rootCount:0L);
        result.put("replyCount",(total!=null?total:0L)-(rootCount!=null?rootCount:0L));
        return result;
    }

    /**
     * 前台：创建评论
     * @param dto 创建参数
     * @param ipAddress IP地址
     * @param userAgent 用户代理信息
     */
    @Override
    @Transactional
    public void createComment(CommentCreateDTO dto, String ipAddress, String userAgent) {
        //1.创建评论信息
        Comment comment = new Comment();
        comment.setArticleId(dto.getArticleId());
        comment.setContent(dto.getContent());
        comment.setNickname(dto.getNickname());
        comment.setEmail(dto.getEmail());
        comment.setParentId(dto.getParentId());
        comment.setRootParentId(dto.getRootParentId());
        comment.setIpAddress(ipAddress);
        comment.setUserAgent(userAgent);

        //2.根据系统设置决定是否需要审核
        SystemSettingVO settings = SysSettingService.getSettings();
        boolean needAudit = settings.getCommentAudit();
        comment.setStatus(needAudit ? PENDING.getCode() : APPROVED.getCode()); // 0-待审核，1-审核通过

        //3.如果有父级评论，则获取被回复人信息
        if (dto.getParentId() != null) {
            Comment parent = getById(dto.getParentId());
            if (parent != null){
                comment.setReplyUserId(parent.getUserId());
            }
        }
    }

    /**
     * 后台：分页查询评论
     *
     * @param dto 查询参数
     * @return 分页结果
     */
    @Override
    public Page<CommentAdminVO> pageComments(CommentPageQueryDTO dto) {
        // 1.构建查询条件
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        if (dto.getStatus() != null) {
            wrapper.eq(Comment::getStatus, dto.getStatus());
        }
        if (dto.getArticleId() != null) {
            wrapper.eq(Comment::getArticleId, dto.getArticleId());
        }
        if (StrUtil.isNotBlank(dto.getNickname())) {
            wrapper.like(Comment::getNickname, dto.getNickname());
        }
        wrapper.orderByDesc(Comment::getCreateTime);

        // 2. 分页查询
        // 2.1 确保分页参数不为null，提供默认值
        int currentPage = dto.getCurrent() != null ? dto.getCurrent() : 1;
        int pageSize = dto.getSize() != null ? dto.getSize() : 10;

        // 2.2 构建分页查询对象
        Page<Comment> page = page(new Page<>(currentPage, pageSize), wrapper);
        Page<CommentAdminVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());

        // 2.3 填充信息
        voPage.setRecords(page.getRecords()
                .stream()
                .map(comment -> {
                    CommentAdminVO vo = BeanUtil.copyProperties(comment, CommentAdminVO.class);
                    // 填充文章标题
                    if (comment.getArticleId() != null) {
                        Article article = articleMapper.selectById(comment.getArticleId());
                        if (article != null) {
                            vo.setArticleTitle(article.getTitle());
                        }
                    } else {
                        vo.setArticleTitle("留言板");
                    }
                    return vo;
                }).toList());
        // 3.返回分页结果
        return voPage;
    }

    /**
     * 审核评论
     *
     * @param id  评论ID
     * @param dto 审核参数
     */
    @Override
    @Transactional
    public void auditComment(Long id, CommentAuditDTO dto) {
        // 1.判断评论是否存在
        Comment comment = getById(id);
        if (comment == null) {
            throw new BusinessException(COMMENT_NOT_FOUND);
        }
        // 2.更新评论状态
        comment.setStatus(dto.getStatus());
        updateById(comment);
    }

    /**
     * 删除评论
     *
     * @param id 评论ID
     */
    @Override
    @Transactional
    public void deleteComment(Long id) {
        // 1.判断评论是否存在
        Comment comment = getById(id);
        if (comment == null) {
            throw new BusinessException(COMMENT_NOT_FOUND);
        }
        // 2.删除评论
        removeById(id);
    }

    /**
     * 批量审核评论
     *
     * @param ids    评论ID列表
     * @param status 审核状态
     */
    @Override
    public void batchAuditComments(List<Long> ids, Integer status) {
        // 1.判断评论ID列表是否为空
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(COMMENT_AUDIT_EMPTY);
        }
        // 2.批量更新评论状态
        lambdaUpdate()
                .in(Comment::getId, ids)
                .set(Comment::getStatus, status)
                .update();
    }

    @Override
    @Transactional
    public void batchDeleteComments(List<Long> ids) {
        // 1.判断评论ID列表是否为空
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(COMMENT_DELETE_EMPTY);
        }
        // 2.批量删除评论
        removeByIds(ids);
    }

    // <=============私有辅助方法=================>

    /**
     * 构建两级评论树形结构（核心内存组装方法）
     * <p>
     * 【职责说明】
     * 仅负责将已分好页、数量可控的两批数据（顶级评论 + 其下的子评论）组装成树形 VO，纯内存操作。
     * 所有数据库查询和分页逻辑均由调用方 getCommentTree 完成。
     * <p>
     * 接收已分页的顶级评论 + 已用 IN 批量查出的子评论，内存占用可控。
     * <p>
     * 【时间复杂度】O(N)，N = 顶级评论数 + 子评论数之和。
     *
     * @param rootComments  当前页的顶级评论（rootParentId IS NULL）
     * @param childComments 本页顶级评论下的全部子评论（通过 IN(rootIds) 查出）
     * @return 树形 VO 列表，顺序与 rootComments 的数据库排序保持一致
     */
    private List<CommentTreeVO> buildCommentTree(List<Comment> rootComments, List<Comment> childComments) {

        // ===== Step0: 预构建所有评论的实体索引，用于 O(1) 查找被回复人昵称 =====
        // 子评论的 parentId 可能指向顶级评论，也可能指向另一条子评论，所以两者都要加入 Map
        Map<Long, Comment> allEntityMap = new HashMap<>(rootComments.size() + childComments.size(), 1.0f);
        for (Comment c : rootComments)
            allEntityMap.put(c.getId(), c);
        for (Comment c : childComments)
            allEntityMap.put(c.getId(), c);

        // ===== Step1: 将顶级评论转为 VO，同时建立 rootId -> rootVO 的快速索引 =====
        Map<Long, CommentTreeVO> rootVOMap = rootComments.stream()
                .collect(Collectors.toMap(
                        Comment::getId,
                        c -> toVO(c, null), // 顶级评论没有"被回复人"
                        (e, r) -> e));

        // ===== Step2: 遍历子评论，填充被回复人昵称，并挂载到对应的根节点下 =====
        for (Comment child : childComments) {
            // parentId 指向当前评论直接回复的那条评论（可能是顶级，也可能是另一条子评论）
            String replyNickname = null;
            if (child.getParentId() != null) {
                Comment parent = allEntityMap.get(child.getParentId());
                if (parent != null) {
                    replyNickname = parent.getNickname();
                }
            }

            CommentTreeVO childVO = toVO(child, replyNickname);

            // rootParentId 即本条子评论所属顶级评论的 ID
            CommentTreeVO rootVO = rootVOMap.get(child.getRootParentId());
            if (rootVO != null) {
                rootVO.getChildren().add(childVO);
            }
            // rootVO 为 null 属于异常数据（IN 查询已限定范围，理论上不会出现），直接跳过
        }

        // ===== Step3: 按 rootComments 的原始顺序返回根节点列表（保持数据库排序）=====
        return rootComments.stream()
                .map(c -> rootVOMap.get(c.getId()))
                .collect(Collectors.toList());
    }

    /**
     * 将 Comment 实体转为 CommentTreeVO
     *
     * @param comment       评论实体
     * @param replyNickname 被回复人昵称（顶级评论传 null）
     * @return CommentTreeVO
     */
    private CommentTreeVO toVO(Comment comment, String replyNickname) {
        CommentTreeVO vo = new CommentTreeVO();
        vo.setId(comment.getId());
        vo.setNickname(comment.getNickname());
        vo.setContent(comment.getContent());
        vo.setCreateTime(comment.getCreateTime());
        vo.setReplyNickname(replyNickname);
        vo.setChildren(new ArrayList<>());
        return vo;
    }
}
