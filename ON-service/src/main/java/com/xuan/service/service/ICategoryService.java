package com.xuan.service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xuan.entity.dto.category.CategoryCreateDTO;
import com.xuan.entity.dto.category.CategoryPageQueryDTO;
import com.xuan.entity.dto.category.CategoryUpdateDTO;
import com.xuan.entity.po.blog.Category;
import com.xuan.entity.vo.category.CategoryAdminListVO;

import java.util.List;

/**
 * 分类服务接口
 */
public interface ICategoryService extends IService<Category> {

    /** 前台：获取所有启用分类（含文章数量） */
    //List<CategoryVO> listAllCategories();

    /** 后台：分页查询分类列表 */
    Page<CategoryAdminListVO> pageCategories(CategoryPageQueryDTO queryDTO);

    /** 后台：创建分类 */
    void createCategory(CategoryCreateDTO createDTO);

    /** 后台：更新分类 */
    void updateCategory(Long id, CategoryUpdateDTO updateDTO);

    /** 后台：删除分类 */
    void deleteCategory(Long id);

    /** 后台：批量删除分类 */
    void batchDeleteCategories(List<Long> ids);
}