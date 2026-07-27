package com.catering.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("table_area")
public class TableArea {

    @TableId(type = IdType.AUTO)
    private Long id;

   private String name;

   private Integer sort;

   private  Integer status;

   @TableField(fill = FieldFill.INSERT)
   private LocalDateTime createTime;
}
