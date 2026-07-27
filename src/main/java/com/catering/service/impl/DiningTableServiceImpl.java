package com.catering.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.catering.entity.DiningTable;
import com.catering.mapper.DiningTableMapper;
import com.catering.service.DiningTableService;
import org.springframework.stereotype.Service;

@Service
public class DiningTableServiceImpl
        extends ServiceImpl<DiningTableMapper, DiningTable>
        implements DiningTableService {

}