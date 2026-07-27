package com.catering.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.catering.entity.SysUser;
import com.catering.mapper.SysUserMapper;
import com.catering.service.SysUserService;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl
        extends ServiceImpl<SysUserMapper, SysUser>
        implements SysUserService {

}