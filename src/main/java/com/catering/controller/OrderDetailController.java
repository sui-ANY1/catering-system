package com.catering.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.catering.common.Result;
import com.catering.entity.OrderDetail;
import com.catering.service.OrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orderDetail")
public class OrderDetailController {

    @Autowired
    private OrderDetailService orderDetailService;

    // 根据订单ID查询订单明细
    @GetMapping("/{orderId}")
    public Result<List<OrderDetail>> listByOrderId(@PathVariable Long orderId) {
        List<OrderDetail> list = orderDetailService.list(
                new LambdaQueryWrapper<OrderDetail>()
                        .eq(OrderDetail::getOrderId, orderId)
                        .orderByAsc(OrderDetail::getId)
        );
        return Result.success(list);
    }
}
