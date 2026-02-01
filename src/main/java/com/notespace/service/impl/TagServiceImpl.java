package com.notespace.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.notespace.entity.NoteTag;
import com.notespace.entity.Tag;
import com.notespace.exception.BusinessException;
import com.notespace.mapper.NoteTagMapper;
import com.notespace.mapper.TagMapper;
import com.notespace.service.TagService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Resource
    private NoteTagMapper noteTagMapper;

    @Override
    public List<Tag> getTagList(Long userId) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getUserId, userId);
        wrapper.orderByDesc(Tag::getCreateTime);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public Tag createTag(Long userId, String name) {
        if (StrUtil.isBlank(name)) {
            throw new BusinessException("标签名称不能为空");
        }

        // 检查是否已存在
        Tag existTag = baseMapper.selectOne(new LambdaQueryWrapper<Tag>()
                .eq(Tag::getUserId, userId)
                .eq(Tag::getName, name));
        if (existTag != null) {
            throw new BusinessException("标签已存在");
        }

        Tag tag = new Tag();
        tag.setUserId(userId);
        tag.setName(name);

        baseMapper.insert(tag);
        return tag;
    }

    @Override
    public void deleteTag(Long userId, Long id) {
        Tag tag = baseMapper.selectById(id);
        if (tag == null) {
            throw new BusinessException("标签不存在");
        }
        if (!tag.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作此标签");
        }

        // 检查是否有笔记使用了该标签
        Long count = noteTagMapper.selectCount(new LambdaQueryWrapper<NoteTag>()
                .eq(NoteTag::getTagId, id));
        if (count > 0) {
            throw new BusinessException("该标签正在被笔记使用，无法删除");
        }

        baseMapper.deleteById(id);
    }

    @Override
    public List<Tag> getTagsByNoteId(Long noteId) {
        return baseMapper.selectList(new LambdaQueryWrapper<Tag>()
                .inSql(Tag::getId, "SELECT tag_id FROM t_note_tag WHERE note_id = " + noteId));
    }
}
