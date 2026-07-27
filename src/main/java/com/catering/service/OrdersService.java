package com.catering.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.catering.entity.Orders;

public interface OrdersService extends IService<Orders> {
    String createOrder(Long tableId);
}