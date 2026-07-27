package com.catering.service.impl;

import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.catering.entity.*;
import com.catering.mapper.*;
import com.catering.service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OrdersServiceImpl
        extends ServiceImpl<OrdersMapper, Orders>
        implements OrdersService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private DiningTableMapper tableMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    private static final String LOCK_KEY = "lock:order:";

    @Override
    @Transactional
    public String createOrder(Long tableId) {
        // 分布式锁
        String lockKey = LOCK_KEY + tableId;
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        if (!locked) {
            throw new RuntimeException("订单处理中，请稍后重试");
        }

        try {
            // 1. 校验桌台
            DiningTable table = tableMapper.selectById(tableId);
            if (table == null || table.getStatus() != 0) {
                throw new RuntimeException("桌台不可用");
            }

            // 2. 从 Redis 取购物车
            String cartKey = "cart:table:" + tableId;
            Map<Object, Object> cartMap = redisTemplate.opsForHash().entries(cartKey);
            if (cartMap.isEmpty()) {
                throw new RuntimeException("购物车为空");
            }

            // 3. 生成订单号
            String orderNo = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                    + String.format("%06d", System.currentTimeMillis() % 1000000);

            // 4. 计算金额 + 校验库存
            BigDecimal totalAmount = BigDecimal.ZERO;
            List<OrderDetail> details = new ArrayList<>();

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            for (Object value : cartMap.values()) {
                Dish dish = mapper.convertValue(value, Dish.class);

                Dish dbDish = dishMapper.selectById(dish.getId());
                if (dbDish == null || dbDish.getStatus() == 0) {
                    throw new RuntimeException("菜品【" + dish.getName() + "】已下架");
                }
                if (dbDish.getStock() < 1) {
                    throw new RuntimeException("菜品【" + dish.getName() + "】库存不足");
                }
                dbDish.setStock(dbDish.getStock() - 1);
                dbDish.setSales(dbDish.getSales() + 1);
                dishMapper.updateById(dbDish);

                OrderDetail detail = new OrderDetail();
                detail.setDishId(dish.getId());
                detail.setDishName(dish.getName());
                detail.setPrice(dish.getPrice());
                detail.setQuantity(1);
                detail.setTotalPrice(dish.getPrice());
                details.add(detail);

                totalAmount = totalAmount.add(dish.getPrice());
            }

            // 5. 创建订单
            Orders order = new Orders();
            order.setOrderNo(orderNo);
            order.setTableId(tableId);
            order.setTableNo(table.getTableNo());
            order.setTotalAmount(totalAmount);
            order.setPayAmount(totalAmount);
            order.setOrderStatus(1);
            order.setPayTime(LocalDateTime.now());
            this.save(order);

            // 6. 保存订单明细
            for (OrderDetail detail : details) {
                detail.setOrderId(order.getId());
                orderDetailMapper.insert(detail);
            }

            // 7. 修改桌台状态
            table.setStatus(1);
            tableMapper.updateById(table);

            // 8. 清空购物车
            redisTemplate.delete(cartKey);

            return orderNo;
        } finally {
            redisTemplate.delete(lockKey);
        }
    }
}