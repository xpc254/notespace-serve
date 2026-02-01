package com.notespace.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.notespace.entity.Tag;

import java.util.List;

public interface TagService extends IService<Tag> {

    List<Tag> getTagList(Long userId);

    Tag createTag(Long userId, String name);

    void deleteTag(Long userId, Long id);

    List<Tag> getTagsByNoteId(Long noteId);
}
