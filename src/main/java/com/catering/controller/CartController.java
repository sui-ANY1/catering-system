package com.catering.controller;

import com.catering.common.Result;
import com.catering.entity.Dish;
import com.catering.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private DishService dishService;

    // 加入购物车
    @PostMapping("/add")
    public Result<?> add(@RequestParam Long tableId,
                         @RequestParam Long dishId) {
        Dish dish = dishService.getById(dishId);
        if (dish == null || dish.getStatus() == 0) {
            return Result.fail("菜品已下架");
        }
        String key = "cart:table:" + tableId;
        redisTemplate.opsForHash().put(key, dishId.toString(), dish);
        return Result.success();
    }

    // 查看购物车
    @GetMapping("/list")
    public Result<List<Object>> list(@RequestParam Long tableId) {
        String key = "cart:table:" + tableId;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        return Result.success(entries.values().stream().toList());
    }

    // 清空购物车
    @DeleteMapping("/clear")
    public Result<?> clear(@RequestParam Long tableId) {
        String key = "cart:table:" + tableId;
        redisTemplate.delete(key);
        return Result.success();
    }
}