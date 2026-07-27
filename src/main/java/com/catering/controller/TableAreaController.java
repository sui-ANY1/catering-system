package com.catering.controller;

import com.catering.common.Result;
import com.catering.entity.TableArea;
import com.catering.service.TableAreaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/area")
public class TableAreaController {

    @Autowired
    private TableAreaService tableAreaService;

    @GetMapping
    public Result<List<TableArea>> list() {
        return Result.success(tableAreaService.list());
    }

    @GetMapping("/{id}")
    public Result<TableArea> getById(@PathVariable Long id) {
        return Result.success(tableAreaService.getById(id));
    }

    @PostMapping
    public Result<?> save(@RequestBody TableArea tableArea) {
        tableAreaService.save(tableArea);
        return Result.success();
    }

    @PutMapping
    public Result<?> update(@RequestBody TableArea tableArea) {
        tableAreaService.updateById(tableArea);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        tableAreaService.removeById(id);
        return Result.success();
    }
}
