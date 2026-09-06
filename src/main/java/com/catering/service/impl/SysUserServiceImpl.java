package com.catering.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.catering.entity.SysUser;
import com.catering.mapper.SysUserMapper;
import com.catering.service.SysUserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SysUserServiceImpl
        extends ServiceImpl<SysUserMapper, SysUser>
        implements SysUserService {

    @Override
    public boolean save(SysUser user) {
        // 新增时对明文密码做 BCrypt 加密
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(BCrypt.hashpw(user.getPassword()));
        }
        return super.save(user);
    }

    @Override
    public boolean updateById(SysUser user) {
        // 密码有值则加密后更新；为空则不修改密码字段
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(BCrypt.hashpw(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        return super.updateById(user);
    }
}
