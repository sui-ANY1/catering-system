package com.catering.controller;

import com.catering.common.Result;
import com.catering.dto.CartItem;
import com.catering.entity.Dish;
import com.catering.service.DishService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String cartKey(Long tableId) {
        return "cart:table:" + tableId;
    }

    private CartItem getItem(String key, Long dishId) {
        Object v = redisTemplate.opsForHash().get(key, dishId.toString());
        return v == null ? null : objectMapper.convertValue(v, CartItem.class);
    }

    // 加入购物车（已存在则数量 +1）
    @PostMapping("/add")
    public Result<?> add(@RequestParam Long tableId, @RequestParam Long dishId) {
        Dish dish = dishService.getById(dishId);
        if (dish == null || dish.getStatus() == 0) {
            return Result.fail("菜品已下架");
        }
        String key = cartKey(tableId);
        CartItem item = getItem(key, dishId);
        if (item == null) {
            item = new CartItem();
            item.setDishId(dish.getId());
            item.setName(dish.getName());
            item.setPrice(dish.getPrice());
            item.setImage(dish.getImage());
            item.setDescription(dish.getDescription());
            item.setQuantity(1);
        } else {
            item.setQuantity(item.getQuantity() + 1);
        }
        redisTemplate.opsForHash().put(key, dishId.toString(), item);
        return Result.success();
    }

    // 减少数量（减到 0 则移除）
    @PostMapping("/decrease")
    public Result<?> decrease(@RequestParam Long tableId, @RequestParam Long dishId) {
        String key = cartKey(tableId);
        CartItem item = getItem(key, dishId);
        if (item == null) {
            return Result.success();
        }
        int qty = item.getQuantity() - 1;
        if (qty <= 0) {
            redisTemplate.opsForHash().delete(key, dishId.toString());
        } else {
            item.setQuantity(qty);
            redisTemplate.opsForHash().put(key, dishId.toString(), item);
        }
        return Result.success();
    }

    // 移除单个菜品
    @DeleteMapping("/remove")
    public Result<?> remove(@RequestParam Long tableId, @RequestParam Long dishId) {
        redisTemplate.opsForHash().delete(cartKey(tableId), dishId.toString());
        return Result.success();
    }

    // 查看购物车
    @GetMapping("/list")
    public Result<List<CartItem>> list(@RequestParam Long tableId) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(cartKey(tableId));
        List<CartItem> items = entries.values().stream()
                .map(v -> objectMapper.convertValue(v, CartItem.class))
                .toList();
        return Result.success(items);
    }

    // 清空购物车
    @DeleteMapping("/clear")
    public Result<?> clear(@RequestParam Long tableId) {
        redisTemplate.delete(cartKey(tableId));
        return Result.success();
    }
}
