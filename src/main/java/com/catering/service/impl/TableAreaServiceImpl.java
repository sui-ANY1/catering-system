package com.catering.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.catering.entity.TableArea;
import com.catering.mapper.TableAreaMapper;
import com.catering.service.TableAreaService;
import org.springframework.stereotype.Service;

@Service
public class TableAreaServiceImpl
        extends ServiceImpl<TableAreaMapper, TableArea>
        implements TableAreaService {

}