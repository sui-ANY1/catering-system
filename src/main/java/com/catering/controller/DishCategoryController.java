package com.catering.controller;

import com.catering.common.Result;
import com.catering.entity.DishCategory;
import com.catering.service.DishCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class DishCategoryController {

    @Autowired
    private DishCategoryService dishCategoryService;

    // 查全部分类
    @GetMapping
    public Result<List<DishCategory>> list() {
        List<DishCategory> list = dishCategoryService.list();
        return Result.success(list);
    }

    // 查单个分类
    @GetMapping("/{id}")
    public Result<DishCategory> getById(@PathVariable Long id) {
        DishCategory category = dishCategoryService.getById(id);
        return Result.success(category);
    }

    // 新增分类
    @PostMapping
    public Result<?> save(@RequestBody DishCategory dishCategory) {
        dishCategoryService.save(dishCategory);
        return Result.success();
    }

    // 修改分类
    @PutMapping
    public Result<?> update(@RequestBody DishCategory dishCategory) {
        dishCategoryService.updateById(dishCategory);
        return Result.success();
    }

    // 删除分类
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        dishCategoryService.removeById(id);
        return Result.success();
    }
}
