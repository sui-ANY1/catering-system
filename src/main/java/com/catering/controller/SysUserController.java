package com.catering.controller;

import com.catering.common.Result;
import com.catering.entity.SysUser;
import com.catering.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @GetMapping
    public Result<List<SysUser>> list() {
        return Result.success(sysUserService.list());
    }

    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        return Result.success(sysUserService.getById(id));
    }

    @PostMapping
    public Result<?> save(@RequestBody SysUser sysUser) {
        sysUserService.save(sysUser);
        return Result.success();
    }

    @PutMapping
    public Result<?> update(@RequestBody SysUser sysUser) {
        sysUserService.updateById(sysUser);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        sysUserService.removeById(id);
        return Result.success();
    }
}