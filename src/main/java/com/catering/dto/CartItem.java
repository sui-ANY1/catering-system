package com.catering.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 购物车条目（Redis Hash 存储的 value）
 */
@Data
public class CartItem {

    private Long dishId;

    private String name;

    private BigDecimal price;

    private String image;

    private String description;

    private Integer quantity;
}
