package com.xuan.service.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuan.entity.po.blog.Article;
import com.xuan.entity.po.interact.Comment;
import com.xuan.service.mapper.ArticleMapper;
import com.xuan.service.mapper.CategoryMapper;
import com.xuan.service.mapper.CommentMapper;
import com.xuan.service.mapper.SysUserMapper;
import com.xuan.service.mapper.TagMapper;
import com.xuan.service.service.IStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.xuan.common.enums.ArticleStatusEnum.PUBLISHED;
import static com.xuan.common.enums.CommentStatusEnum.APPROVED;

/**
 * 站点统计服务实现类
 * 数据来源：
 * - 文章/分类/标签/评论/用户数量：数据库查询
 * - 浏览量/UV/PV：Redis 实时统计（通过 IVisitLogService）
 * - 访问趋势：数据库 visit_log 表聚合
 *
 * @author 玄〤
 * @since 2026-02-20
 */
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements IStatisticsService {

    private final ArticleMapper articleMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final CommentMapper commentMapper;
    private final SysUserMapper sysUserMapper;

    /**
     * 获取站点概览数据
     * @return 站点概览数据
     */
    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> result=new LinkedHashMap<>();

        //1.基础计数
        result.put("articleCount", articleMapper.selectCount(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getStatus,PUBLISHED)));//已发布文章数量
        result.put("categoryCount",categoryMapper.selectCount(null));//分类数量
        result.put("tagCount",tagMapper.selectCount(null));//标签数量
        result.put("commentCount",commentMapper.selectCount(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getStatus,APPROVED)));//已审核的评论数量
        result.put("userCount",sysUserMapper.selectCount(null));//用户数量

        //2.文章总览量
        long totalViewCount = articleMapper.selectList(
                        new LambdaQueryWrapper<Article>()
                                .eq(Article::getStatus, PUBLISHED)
                                .select(Article::getViewCount))
                .stream()
                .mapToLong(article -> article.getViewCount() != null ? article.getViewCount() : 0)
                .sum();
        result.put("totalViewCount", totalViewCount);

        //3.返回结果
        return result;
    }

    /**
     * 获取文章趋势数据
     * @return 文章趋势数据
     */
    @Override
    public Map<String, Object> getArticleTrend() {
        Map<String, Object> result=new LinkedHashMap<>();
        //1.过去七天每天发布的文章数
        List<String>labels=new ArrayList<>();
        List<Long> data=new ArrayList<>();

        for (int i=6;i>=0;i--){
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(LocalTime.MAX);
            labels.add(date.toString());
            Long count = articleMapper.selectCount(
                    new LambdaQueryWrapper<Article>()
                            .between(Article::getCreateTime, start, end)
                            .eq(Article::getStatus, PUBLISHED));
            data.add(count);
        }

        //2.将得到的数据添加到结果当中
        result.put("labels",labels);
        result.put("data",data);
        //3.返回结果
        return result;
    }

    @Override
    public Map<String, Object> getVisitStats() {
        Map<String, Object> result = new LinkedHashMap<>();

        //1.获取总访问量

        return result;
    }
}
