package com.catering.controller;

import com.catering.common.Result;
import com.catering.entity.DiningTable;
import com.catering.service.DiningTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/table")
public class DiningTableController {

    @Autowired
    private DiningTableService diningTableService;

    @GetMapping
    public Result<List<DiningTable>> list() {
        return Result.success(diningTableService.list());
    }

    @GetMapping("/{id}")
    public Result<DiningTable> getById(@PathVariable Long id) {
        return Result.success(diningTableService.getById(id));
    }

    @PostMapping
    public Result<?> save(@RequestBody DiningTable diningTable) {
        diningTableService.save(diningTable);
        return Result.success();
    }

    @PutMapping
    public Result<?> update(@RequestBody DiningTable diningTable) {
        diningTableService.updateById(diningTable);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        diningTableService.removeById(id);
        return Result.success();
    }
}