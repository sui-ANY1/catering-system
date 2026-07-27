package com.catering.controller;

import com.catering.common.Result;
import com.catering.entity.Member;
import com.catering.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @GetMapping
    public Result<List<Member>> list() {
        return Result.success(memberService.list());
    }

    @GetMapping("/{id}")
    public Result<Member> getById(@PathVariable Long id) {
        return Result.success(memberService.getById(id));
    }

    @PostMapping
    public Result<?> save(@RequestBody Member member) {
        memberService.save(member);
        return Result.success();
    }

    @PutMapping
    public Result<?> update(@RequestBody Member member) {
        memberService.updateById(member);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        memberService.removeById(id);
        return Result.success();
    }
}