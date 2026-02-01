package com.notespace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@TableName("t_note")
public class Note {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long notebookId;

    private String title;

    private String content;

    private String preview;

    private Integer isPinned;

    private Integer isShared;

    /**
     * 分享ID - 用于公开访问笔记
     */
    private String shareId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private List<String> tags = new ArrayList<>();
}
