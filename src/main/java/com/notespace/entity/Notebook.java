package com.notespace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_notebook")
public class Notebook {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    private String icon;

    private String color;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
