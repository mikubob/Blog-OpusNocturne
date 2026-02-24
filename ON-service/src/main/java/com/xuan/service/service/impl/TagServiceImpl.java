package com.xuan.service.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuan.common.exceptions.BusinessException;
import com.xuan.entity.dto.tag.TagDTO;
import com.xuan.entity.dto.tag.TagPageQueryDTO;
import com.xuan.entity.po.blog.ArticleTag;
import com.xuan.entity.po.blog.Tag;
import com.xuan.entity.vo.tag.TagAdminVO;
import com.xuan.service.mapper.ArticleTagMapper;
import com.xuan.service.mapper.TagMapper;
import com.xuan.service.service.ITagService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.xuan.common.constant.RedisConstant.TAG_LIST_KEY;
import static com.xuan.common.enums.ErrorCode.CATEGORY_HAS_ARTICLES;
import static com.xuan.common.enums.ErrorCode.TAG_DELETE_EMPTY;
import static com.xuan.common.enums.ErrorCode.TAG_EXISTS;
import static com.xuan.common.enums.ErrorCode.TAG_NOT_FOUND;

/**
 * 标签服务实现类
 * 前台标签列表使用 Redis 缓存，后台操作时主动清除缓存
 *
 * @author 玄〤
 * @since 2026-02-20
 */
@Service
@RequiredArgsConstructor
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements ITagService {

    private final StringRedisTemplate redisTemplate;
    private final ArticleTagMapper articleTagMapper;

    /**
     * 分页获取标签列表
     * @param queryDTO 查询参数
     * @return 分页标签列表
     */
    @Override
    public Page<TagAdminVO> pageTags(TagPageQueryDTO queryDTO) {

        //1. 创建查询条件
        LambdaQueryWrapper<Tag> wrapper=new LambdaQueryWrapper<>();
        if (queryDTO.getName()!=null&& !queryDTO.getName().trim().isEmpty()){
            wrapper.like(Tag::getName,queryDTO.getName());
        }
        wrapper.orderByDesc(Tag::getId);

        //2. 分页查询
        //2.1 确保分页参数部位null，提供默认值
        int current=queryDTO.getCurrent()!=null?queryDTO.getCurrent():1;
        int size=queryDTO.getSize()!=null?queryDTO.getSize():10;
        //2.2 创建分页对象
        Page<Tag> page = page(new Page<>(current, size), wrapper);
        Page<TagAdminVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        //2.3 将分页结果转换为VO
        voPage.setRecords(page.getRecords()
                .stream()
                .map(tag -> BeanUtil.copyProperties(tag, TagAdminVO.class))
                .toList());
        //3. 返回分页结果
        return voPage;
    }

    /**
     * 创建标签
     * @param tagDTO 标签信息
     */
    @Override
    @Transactional
    public void createTag(TagDTO tagDTO) {
        //1.判断要创建的标签是否存在
        long count = count(new LambdaQueryWrapper<Tag>().eq(Tag::getName, tagDTO.getName()));
        if (count>0){
            throw new BusinessException(TAG_EXISTS);
        }
        //2.创建标签
        Tag tag = BeanUtil.copyProperties(tagDTO, Tag.class);
        save(tag);

        //3.清除缓存
        redisTemplate.delete(TAG_LIST_KEY);
    }

    /**
     * 更新标签
     * @param id 标签ID
     * @param tagDTO 标签信息
     */
    @Override
    @Transactional
    public void updateTag(Long id, TagDTO tagDTO) {
        //1.判断要更新的标签是否存在
        Tag tag = getById(id);
        if (tag==null){
            throw new BusinessException(TAG_NOT_FOUND);
        }
        //2.判断其他标签是否使用了相同的名称
        long count = count(new LambdaQueryWrapper<Tag>()
                .eq(Tag::getName, tagDTO.getName())
                .ne(Tag::getId, id));
        if (count>0){
            throw new BusinessException(TAG_EXISTS);
        }
        //2.更新标签
        tag = BeanUtil.copyProperties(tagDTO, Tag.class);
        updateById(tag);
        //3.清除缓存
        redisTemplate.delete(TAG_LIST_KEY);
    }

    /**
     * 删除标签
     * @param id 标签ID
     */
    @Override
    @Transactional
    public void deleteTag(Long id) {
        //1.判断要删除的标签是否存在
        Tag tag = getById(id);
        if (tag==null){
            throw new BusinessException(TAG_NOT_FOUND);
        }
        //2.判断该标签下是否还有文章
        Long count = articleTagMapper.selectCount(
                new LambdaQueryWrapper<ArticleTag>()
                        .eq(ArticleTag::getTagId, id));
        if (count>0){
            throw new BusinessException(CATEGORY_HAS_ARTICLES);
        }
        //3.删除标签
        removeById(id);
        //4.清除缓存
        redisTemplate.delete(TAG_LIST_KEY);
    }

    /**
     * 批量删除标签
     * @param ids 标签ID列表
     */
    @Override
    @Transactional
    public void batchDeleteTags(List<Long> ids) {
        //1.判断要删除的标签列表是否为空
        if (ids==null||ids.isEmpty()) {
            throw new BusinessException(TAG_DELETE_EMPTY);
        }
        //2.检查每个标签是否有关联文章
        for (Long id : ids) {
            Long count = articleTagMapper.selectCount(
                    new LambdaQueryWrapper<ArticleTag>()
                            .eq(ArticleTag::getTagId, id));
            if (count>0){
                Tag tag = getById(id);
                String name= tag!=null ?tag.getName():String.valueOf(id);
                throw new BusinessException(CATEGORY_HAS_ARTICLES.getCode(),
                        "标签【" + name + "】有关联文章，无法删除");
            }
        }
        //3.批量删除标签
        removeByIds(ids);
        //4.清除缓存
        redisTemplate.delete(TAG_LIST_KEY);
    }
}
