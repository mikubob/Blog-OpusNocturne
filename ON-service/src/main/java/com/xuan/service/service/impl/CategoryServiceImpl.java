package com.xuan.service.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuan.common.enums.ErrorCode;
import com.xuan.common.exceptions.BusinessException;
import com.xuan.entity.dto.category.CategoryCreateDTO;
import com.xuan.entity.dto.category.CategoryPageQueryDTO;
import com.xuan.entity.dto.category.CategoryUpdateDTO;
import com.xuan.entity.po.blog.Article;
import com.xuan.entity.po.blog.Category;
import com.xuan.entity.vo.category.CategoryAdminListVO;
import com.xuan.entity.vo.category.CategoryVO;
import com.xuan.service.mapper.ArticleMapper;
import com.xuan.service.mapper.CategoryMapper;
import com.xuan.service.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.xuan.common.constant.RedisConstant.CATEGORY_LIST_KEY;
import static com.xuan.common.constant.RedisConstant.CATEGORY_TAG_TTL_HOURS;
import static com.xuan.common.enums.ArticleStatusEnum.PUBLISHED;
import static com.xuan.common.enums.ErrorCode.CATEGORY_DELETE_EMPTY;
import static com.xuan.common.enums.ErrorCode.CATEGORY_EXISTS;
import static com.xuan.common.enums.ErrorCode.CATEGORY_HAS_ARTICLES;
import static com.xuan.common.enums.ErrorCode.CATEGORY_NOT_FOUND;
import static io.lettuce.core.protocol.CommandType.PUBLISH;

/**
 * 分类服务实现类
 * 前台分类列表使用 Redis 缓存，后台操作时主动清除缓存
 *
 * @author 玄〤
 * @since 2026-02-20
 */

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

    private final ArticleMapper articleMapper;
    private final StringRedisTemplate redisTemplate;

    /**
     * 前台获取所有分类
     * @return 所有分类的列表
     */

    @Override
    public List<CategoryVO> listAllCategories() {
        //1.尝试用Redis中读取缓存
        String cached = redisTemplate.opsForValue().get(CATEGORY_LIST_KEY);
        if (cached!=null){
            return JSON.parseObject(cached, new TypeReference<List<CategoryVO>>() {});
        }

        //2.缓存未命中，查询数据库
        List<Category> categories = list(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, PUBLISHED)
                .orderByAsc(Category::getSort));
        List<CategoryVO> voList = categories.stream().map(category -> {
            CategoryVO categoryVO = new CategoryVO();
            categoryVO.setId(category.getId());
            categoryVO.setName(category.getName());
            //统计该分类下已发布的文章数量
            Long count = articleMapper.selectCount(
                    new LambdaQueryWrapper<Article>()
                            .eq(Article::getCategoryId, category.getId())
                            .eq(Article::getStatus, PUBLISHED));
            categoryVO.setArticleCount(count.intValue());
            return categoryVO;
        }).toList();

        //3.回填缓存
        redisTemplate.opsForValue().set(CATEGORY_LIST_KEY, JSON.toJSONString(voList),CATEGORY_TAG_TTL_HOURS, TimeUnit.HOURS);

        //4.返回
        return voList;
    }

    /**
     * 后台分类列表
     * @param queryDTO 查询参数
     * @return 分类列表
     */
    @Override
    public Page<CategoryAdminListVO> pageCategories(CategoryPageQueryDTO queryDTO) {

        //1.填充查询条件
        LambdaQueryWrapper<Category> wrapper=new LambdaQueryWrapper<>();
        if(!queryDTO.getName().trim().isEmpty()&&queryDTO.getName()==null){
            wrapper.like(Category::getName,queryDTO.getName());
        }
        wrapper.orderByAsc(Category::getSort);

        //2.分页查询
        //2.1 确保分页参数部位null，提供默认值
        int current=queryDTO.getCurrent()!=null?queryDTO.getCurrent():1;
        int size=queryDTO.getSize()!=null?queryDTO.getSize():10;
        //2.2 创建分页对象
        Page<Category> page=new Page<>(current,size);
        Page<CategoryAdminListVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());

        //2.3 将分页对象中的数据转换成VO
        List<CategoryAdminListVO> voList = page.getRecords().stream()
                .map(category -> BeanUtil.copyProperties(category, CategoryAdminListVO.class))
                .toList();
        voPage.setRecords(voList);

        //3.返回分类列表vo
        return voPage;
    }

    /**
     * 创建分类
     * @param createDTO 创建参数
     */
    @Override
    @Transactional
    public void createCategory(CategoryCreateDTO createDTO) {
        //1.查询当前分类名字是否存在
        long count = count(new LambdaQueryWrapper<Category>()
                .eq(Category::getName, createDTO.getName()));
        if (count>0){
            throw new BusinessException(CATEGORY_EXISTS);
        }

        //2.创建分类
        Category category = BeanUtil.copyProperties(createDTO, Category.class);
        if (category.getSort()==null){
            category.setSort(0);
        }
        if (category.getStatus()==null){
            category.setStatus(1);
        }
        save(category);

        // 3.清除缓存
        redisTemplate.delete(CATEGORY_LIST_KEY);
    }

    /**
     * 更新分类
     * @param id 分类id
     * @param updateDTO 更新参数
     */
    @Override
    @Transactional
    public void updateCategory(Long id, CategoryUpdateDTO updateDTO) {
        //1.查询分类并且判断是否存在
        Category category = getById(id);
        if (category==null){
            throw new BusinessException(CATEGORY_NOT_FOUND);
        }
        //2.判断更新后的分类名称是否被使用
        long count = count(new LambdaQueryWrapper<Category>()
                .eq(Category::getName, updateDTO.getName()));
        if (count>0){
            throw new BusinessException(CATEGORY_EXISTS);
        }
        //3.更新分类
        BeanUtil.copyProperties(updateDTO,category);
        updateById(category);

        //4.清除缓存
        redisTemplate.delete(CATEGORY_LIST_KEY);
    }

    /**
     * 删除分类
     * @param id 分类id
     */
    @Override
    @Transactional
    public void deleteCategory(Long id) {
        //1.查询分类并且判断是否存在
        Category category = getById(id);
        if (category==null){
            throw new BusinessException(CATEGORY_NOT_FOUND);
        }
        //2.判断分类下是否存在文章
        long count = articleMapper.selectCount(new LambdaQueryWrapper<Article>().eq(Article::getCategoryId, id));
        if (count>0){
            throw new BusinessException(CATEGORY_HAS_ARTICLES);
        }
        //3.删除分类
        removeById(id);
        //4.清除缓存
        redisTemplate.delete(CATEGORY_LIST_KEY);
    }

    /**
     * 批量删除分类
     * @param ids id集合
     */
    @Override
    @Transactional
    public void batchDeleteCategories(List<Long> ids) {
        //1.判断id集合中是否存在分类
        if (ids.isEmpty()||ids==null){
            throw new BusinessException(CATEGORY_DELETE_EMPTY);
        }
        //2.检查每个分类下是否存在文章
        for (Long id : ids) {
            long count = articleMapper.selectCount(new LambdaQueryWrapper<Article>().eq(Article::getCategoryId, id));
            if (count>0){
                Category category = getById(id);
                String name = category != null ? category.getName() : String.valueOf(id);
                throw new BusinessException(ErrorCode.CATEGORY_HAS_ARTICLES.getCode(),
                        "分类【" + name + "】下存在文章，无法删除");
            }
        }
        //3.批量删除分类
        removeBatchByIds(ids);
        //4.清除缓存
        redisTemplate.delete(CATEGORY_LIST_KEY);
    }
}
