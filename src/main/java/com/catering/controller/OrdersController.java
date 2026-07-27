package com.catering.controller;

import com.catering.common.Result;
import com.catering.entity.Orders;
import com.catering.service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    @GetMapping
    public Result<List<Orders>> list() {
        return Result.success(ordersService.list());
    }

    @GetMapping("/{id}")
    public Result<Orders> getById(@PathVariable Long id) {
        return Result.success(ordersService.getById(id));
    }

    @PostMapping
    public Result<?> save(@RequestBody Orders orders) {
        ordersService.save(orders);
        return Result.success();
    }

    @PutMapping
    public Result<?> update(@RequestBody Orders orders) {
        ordersService.updateById(orders);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        ordersService.removeById(id);
        return Result.success();
    }

    @PostMapping("/submit")
    public Result<String> submit(@RequestParam Long tableId) {
        String orderNo = ordersService.createOrder(tableId);
        return Result.success(orderNo);
    }
}