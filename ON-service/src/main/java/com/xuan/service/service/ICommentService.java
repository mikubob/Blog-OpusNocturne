package com.xuan.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xuan.entity.dto.comment.CommentAuditDTO;
import com.xuan.entity.dto.comment.CommentPageQueryDTO;
import com.xuan.entity.po.interact.Comment;
import com.xuan.entity.vo.comment.CommentAdminVO;

import java.util.List;
import java.util.Map;

/**
 * 评论服务接口
 */
public interface ICommentService extends IService<Comment> {

    /** 前台：获取文章评论树 */
    //List<CommentTreeVO> getCommentTree(Long articleId);

    /**
     * 获取文章评论统计
     * 
     * @param articleId 文章ID
     * @return 统计Map
     */
    //TODO Map<String, Long> getArticleCommentStats(Long articleId);

    /** 前台：发表评论 */
    //void createComment(CommentCreateDTO dto, String ipAddress, String userAgent);

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