package com.catering.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_detail")
public class OrderDetail {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long dishId;

    private String dishName;

    private BigDecimal price;

    private Integer quantity;

    private BigDecimal totalPrice;

    private String tasteRemark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}