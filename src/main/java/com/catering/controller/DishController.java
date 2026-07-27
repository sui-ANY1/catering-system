package com.catering.controller;

import com.catering.common.Result;
import com.catering.entity.Dish;
import com.catering.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @GetMapping
    public Result<List<Dish>> list() {
        return Result.success(dishService.list());
    }

    @GetMapping("/{id}")
    public Result<Dish> getById(@PathVariable Long id) {
        return Result.success(dishService.getById(id));
    }

    @PostMapping
    public Result<?> save(@RequestBody Dish dish) {
        dishService.save(dish);
        return Result.success();
    }

    @PutMapping
    public Result<?> update(@RequestBody Dish dish) {
        dishService.updateById(dish);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        dishService.removeById(id);
        return Result.success();
    }
}