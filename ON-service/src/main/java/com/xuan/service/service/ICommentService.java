package com.xuan.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xuan.entity.dto.comment.CommentAuditDTO;
import com.xuan.entity.dto.comment.CommentCreateDTO;
import com.xuan.entity.dto.comment.CommentPageQueryDTO;
import com.xuan.entity.po.interact.Comment;
import com.xuan.entity.vo.comment.CommentAdminVO;
import com.xuan.entity.vo.comment.CommentPageVO;

import java.util.List;
import java.util.Map;

/**
 * 评论服务接口
 */
public interface ICommentService extends IService<Comment> {

    /**
     * 前台：分页获取文章评论树
     * <p>
     * 采用两级分页策略：
     * 1. 外层分页：查当前页的顶级评论（rootParentId IS NULL）
     * 2. 内层批量查询：用 IN(rootIds) 查出所有子评论后组装到树中
     *
     * @param articleId 文章ID
     * @param current   当前页码（从 1 开始）
     * @param size      每页顶级评论数量
     * @return 分页评论树
     */
    CommentPageVO getCommentTree(Long articleId, int current, int size);

    /**
     * 获取文章评论统计
     * 
     * @param articleId 文章ID
     * @return 统计Map
     */
    Map<String, Long> getArticleCommentStats(Long articleId);

    /** 前台：发表评论 */
    void createComment(CommentCreateDTO dto, String ipAddress, String userAgent);

    /** 后台：分页查询评论 */
    Page<CommentAdminVO> pageComments(CommentPageQueryDTO dto);

    /** 后台：审核评论 */
    void auditComment(Long id, CommentAuditDTO dto);

    /** 后台：删除评论 */
    void deleteComment(Long id);

    /** 后台：批量审核评论 */
    void batchAuditComments(List<Long> ids, Integer status);

    /** 后台：批量删除评论 */
    void batchDeleteComments(List<Long> ids);
}