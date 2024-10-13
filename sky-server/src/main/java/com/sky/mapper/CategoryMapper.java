package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CategoryMapper {

    /**
     * 新增分类
     *
     * @param category
     */
    void insert(Category category);

    /**
     * 分类分页查询
     *
     * @param categoryPageQueryDTO
     * @return
     */
    // TODO 完善分页查询的需求，需要提供模糊匹配的功能
    Page<Category> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);
}
