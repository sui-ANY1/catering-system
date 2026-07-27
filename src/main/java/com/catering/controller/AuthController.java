package com.catering.controller;

import cn.hutool.crypto.digest.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.catering.common.Result;
import com.catering.dto.LoginDTO;
import com.catering.entity.SysUser;
import com.catering.service.SysUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private SysUserService sysUserService;

    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDTO loginDTO) {
        SysUser user = sysUserService.getOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, loginDTO.getUsername())
        );

        if (user == null) {
            return Result.fail("用户名或密码错误");
        }

        // ⬇ 改成 BCrypt 校验
        if (!BCrypt.checkpw(loginDTO.getPassword(), user.getPassword())) {
            return Result.fail("用户名或密码错误");
        }

        if (user.getStatus() == 0) {
            return Result.fail("账号已被禁用");
        }

        StpUtil.login(user.getId());
        return Result.success(StpUtil.getTokenValue());
    }

    @GetMapping("/logout")
    public Result<?> logout() {
        StpUtil.logout();
        return Result.success();
    }
}