package com.notespace.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_note_tag")
public class NoteTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long noteId;

    private Long tagId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
