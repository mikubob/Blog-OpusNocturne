package com.xuan.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xuan.entity.dto.tag.TagDTO;
import com.xuan.entity.dto.tag.TagPageQueryDTO;
import com.xuan.entity.po.blog.Tag;
import com.xuan.entity.vo.tag.TagAdminVO;
import com.xuan.entity.vo.tag.TagVO;

import java.util.List;

/**
 * 标签服务接口
 */
public interface ITagService extends IService<Tag> {

    /** 前台：获取所有标签（含文章数量） */
    //List<TagVO> listAllTags();

    /** 后台：分页查询标签列表 */
    Page<TagAdminVO> pageTags(TagPageQueryDTO queryDTO);

    /** 后台：创建标签 */
    void createTag(TagDTO tagDTO);

    /** 后台：更新标签 */
    void updateTag(Long id, TagDTO tagDTO);

    /** 后台：删除标签 */
    void deleteTag(Long id);

    /** 后台：批量删除标签 */
    void batchDeleteTags(List<Long> ids);
}