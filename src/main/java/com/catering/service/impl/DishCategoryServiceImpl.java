package com.catering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.catering.entity.Dish;
import com.catering.entity.DishCategory;
import com.catering.mapper.DishCategoryMapper;
import com.catering.mapper.DishMapper;
import com.catering.service.DishCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
public class DishCategoryServiceImpl
        extends ServiceImpl<DishCategoryMapper, DishCategory>
        implements DishCategoryService {

    @Autowired
    private DishMapper dishMapper;

    // 1. 参数类型改为 Serializable，与父类保持一致
    @Override
    public boolean removeById(Serializable id) {
        // 2. 将 Serializable 转换为 Long
        Long categoryId = (Long) id;

        long count = dishMapper.selectCount(
                new LambdaQueryWrapper<Dish>().eq(Dish::getCategoryId, categoryId)
        );
        if (count > 0) {
            throw new RuntimeException("该分类下存在菜品，无法删除");
        }
        return super.removeById(id);
    }
}
