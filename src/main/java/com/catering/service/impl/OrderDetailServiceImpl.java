package com.catering.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.catering.entity.OrderDetail;
import com.catering.mapper.OrderDetailMapper;
import com.catering.service.OrderDetailService;
import org.springframework.stereotype.Service;

@Service
public class OrderDetailServiceImpl
        extends ServiceImpl<OrderDetailMapper, OrderDetail>
        implements OrderDetailService {

}