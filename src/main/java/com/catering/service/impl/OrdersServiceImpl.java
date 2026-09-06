package com.catering.service.impl;

import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.catering.dto.CartItem;
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
            // 1. 校验桌台（仅维修状态不可用，占用中允许追加点餐）
            DiningTable table = tableMapper.selectById(tableId);
            if (table == null || table.getStatus() == 2) {
                throw new RuntimeException("桌台不可用（维修中）");
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
                CartItem item = mapper.convertValue(value, CartItem.class);
                int quantity = (item.getQuantity() == null || item.getQuantity() < 1)
                        ? 1 : item.getQuantity();

                Dish dbDish = dishMapper.selectById(item.getDishId());
                if (dbDish == null || dbDish.getStatus() == 0) {
                    throw new RuntimeException("菜品【" + item.getName() + "】已下架");
                }
                if (dbDish.getStock() < quantity) {
                    throw new RuntimeException("菜品【" + item.getName() + "】库存不足");
                }
                dbDish.setStock(dbDish.getStock() - quantity);
                dbDish.setSales(dbDish.getSales() + quantity);
                dishMapper.updateById(dbDish);

                BigDecimal totalPrice = item.getPrice().multiply(BigDecimal.valueOf(quantity));

                OrderDetail detail = new OrderDetail();
                detail.setDishId(item.getDishId());
                detail.setDishName(item.getName());
                detail.setPrice(item.getPrice());
                detail.setQuantity(quantity);
                detail.setTotalPrice(totalPrice);
                details.add(detail);

                totalAmount = totalAmount.add(totalPrice);
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

            // 7. 桌台空闲时才置为占用（占用中追加点餐不重复变更）
            if (table.getStatus() == 0) {
                table.setStatus(1);
                tableMapper.updateById(table);
            }

            // 8. 清空购物车
            redisTemplate.delete(cartKey);

            return orderNo;
        } finally {
            redisTemplate.delete(lockKey);
        }
    }
}