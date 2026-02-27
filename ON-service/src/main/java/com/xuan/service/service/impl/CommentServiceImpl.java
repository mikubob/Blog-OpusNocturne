package com.xuan.service.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuan.common.exceptions.BusinessException;
import com.xuan.entity.dto.comment.CommentAuditDTO;
import com.xuan.entity.dto.comment.CommentPageQueryDTO;
import com.xuan.entity.po.blog.Article;
import com.xuan.entity.po.interact.Comment;
import com.xuan.entity.vo.comment.CommentAdminVO;
import com.xuan.service.mapper.ArticleMapper;
import com.xuan.service.mapper.CommentMapper;
import com.xuan.service.service.ICommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    /**
     * 后台：分页查询评论
     * @param dto 查询参数
     * @return 分页结果
     */
    @Override
    public Page<CommentAdminVO> pageComments(CommentPageQueryDTO dto) {
        //1.构建查询条件
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        if (dto.getStatus() != null){
            wrapper.eq(Comment::getStatus, dto.getStatus());
        }
        if (dto.getArticleId() != null){
            wrapper.eq(Comment::getArticleId, dto.getArticleId());
        }
        if (StrUtil.isNotBlank(dto.getNickname())){
            wrapper.like(Comment::getNickname, dto.getNickname());
        }
        wrapper.orderByDesc(Comment::getCreateTime);

        //2. 分页查询
        //2.1 确保分页参数不为null，提供默认值
        int currentPage = dto.getCurrent() != null ? dto.getCurrent() : 1;
        int pageSize = dto.getSize() != null ? dto.getSize() : 10;

        //2.2 构建分页查询对象
        Page<Comment> page = page(new Page<>(currentPage, pageSize),wrapper);
        Page<CommentAdminVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());

        //2.3 填充信息
        voPage.setRecords(page.getRecords()
                .stream()
                .map(comment -> {
                    CommentAdminVO vo = BeanUtil.copyProperties(comment, CommentAdminVO.class);
                    // 填充文章标题
                    if (comment.getArticleId() != null){
                        Article article = articleMapper.selectById(comment.getArticleId());
                        if (article != null){
                            vo.setArticleTitle(article.getTitle());
                        }
                    }else{
                        vo.setArticleTitle("留言板");
                    }
                    return vo;
                }).toList());
        //3.返回分页结果
        return voPage;
    }

    /**
     * 审核评论
     * @param id 评论ID
     * @param dto 审核参数
     */
    @Override
    @Transactional
    public void auditComment(Long id, CommentAuditDTO dto) {
        //1.判断评论是否存在
        Comment comment = getById(id);
        if (comment == null){
            throw new BusinessException(COMMENT_NOT_FOUND);
        }
        //2.更新评论状态
        comment.setStatus(dto.getStatus());
        updateById(comment);
    }

    /**
     * 删除评论
     * @param id 评论ID
     */
    @Override
    @Transactional
    public void deleteComment(Long id) {
        //1.判断评论是否存在
        Comment comment = getById(id);
        if (comment == null){
            throw new BusinessException(COMMENT_NOT_FOUND);
        }
        //2.删除评论
        removeById(id);
    }

    /**
     * 批量审核评论
     * @param ids 评论ID列表
     * @param status 审核状态
     */
    @Override
    public void batchAuditComments(List<Long> ids, Integer status) {
        //1.判断评论ID列表是否为空
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(COMMENT_AUDIT_EMPTY);
        }
        //2.批量更新评论状态
        lambdaUpdate()
                .in(Comment::getId, ids)
                .set(Comment::getStatus, status)
                .update();
    }

    @Override
    @Transactional
    public void batchDeleteComments(List<Long> ids) {
        //1.判断评论ID列表是否为空
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(COMMENT_DELETE_EMPTY);
        }
        //2.批量删除评论
        removeByIds(ids);
    }
}
