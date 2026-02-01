package com.notespace.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.notespace.entity.Note;
import com.notespace.entity.NoteTag;
import com.notespace.entity.Tag;
import com.notespace.exception.BusinessException;
import com.notespace.mapper.NoteMapper;
import com.notespace.mapper.NoteTagMapper;
import com.notespace.mapper.TagMapper;
import com.notespace.service.NoteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements NoteService {

    @Resource
    private TagMapper tagMapper;

    @Resource
    private NoteTagMapper noteTagMapper;

    @Override
    public IPage<Note> getNoteList(Long userId, Long notebookId, Integer page, Integer size) {
        Page<Note> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId);
        if (notebookId != null) {
            wrapper.eq(Note::getNotebookId, notebookId);
        }
        wrapper.orderByDesc(Note::getIsPinned);
        wrapper.orderByDesc(Note::getUpdateTime);
        IPage<Note> notePage = baseMapper.selectPage(pageParam, wrapper);
        // 为每个笔记加载标签
        for (Note note : notePage.getRecords()) {
            loadTagsForNote(note);
        }
        return notePage;
    }

    @Override
    public Note getNoteDetail(Long userId, Long id) {
        Note note = baseMapper.selectById(id);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("无权限查看此笔记");
        }
        loadTagsForNote(note);
        return note;
    }

    @Override
    @Transactional
    public Note createNote(Long userId, Long notebookId, String title, String content, List<String> tags) {
        Note note = new Note();
        note.setUserId(userId);
        note.setNotebookId(notebookId);
        note.setTitle(StrUtil.blankToDefault(title, ""));
        note.setContent(content);
        note.setPreview(generatePreview(content));
        note.setIsPinned(0);
        note.setIsShared(0);

        baseMapper.insert(note);

        // 处理标签
        if (tags != null && !tags.isEmpty()) {
            saveTags(userId, note.getId(), tags);
        }

        return note;
    }

    @Override
    @Transactional
    public void updateNote(Long userId, Long id, String title, String content, List<String> tags) {
        Note note = baseMapper.selectById(id);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作此笔记");
        }

        if (StrUtil.isNotBlank(title)) {
            note.setTitle(title);
        }
        if (content != null) {
            note.setContent(content);
            note.setPreview(generatePreview(content));
        }

        baseMapper.updateById(note);

        // 更新标签
        if (tags != null) {
            // 删除旧的标签关联
            noteTagMapper.delete(new LambdaQueryWrapper<NoteTag>()
                    .eq(NoteTag::getNoteId, id));
            // 保存新标签
            saveTags(userId, id, tags);
        }
    }

    @Override
    public void deleteNote(Long userId, Long id) {
        Note note = baseMapper.selectById(id);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作此笔记");
        }

        // 删除笔记的标签关联
        noteTagMapper.delete(new LambdaQueryWrapper<NoteTag>()
                .eq(NoteTag::getNoteId, id));

        baseMapper.deleteById(id);
    }

    @Override
    public void togglePin(Long userId, Long id) {
        Note note = baseMapper.selectById(id);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作此笔记");
        }

        note.setIsPinned(note.getIsPinned() == 0 ? 1 : 0);
        baseMapper.updateById(note);
    }

    @Override
    public IPage<Note> searchNotes(Long userId, String keyword, Integer page, Integer size) {
        Page<Note> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId);
        wrapper.and(w -> w
                .like(Note::getTitle, keyword)
                .or()
                .like(Note::getContent, keyword)
        );
        wrapper.orderByDesc(Note::getUpdateTime);
        IPage<Note> notePage = baseMapper.selectPage(pageParam, wrapper);
        // 为每个笔记加载标签
        for (Note note : notePage.getRecords()) {
            loadTagsForNote(note);
        }
        return notePage;
    }

    @Override
    @Transactional
    public String shareNote(Long userId, Long id) {
        Note note = baseMapper.selectById(id);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作此笔记");
        }

        // 如果已经分享，直接返回现有的分享ID
        if (note.getIsShared() == 1 && StrUtil.isNotBlank(note.getShareId())) {
            return note.getShareId();
        }

        // 生成唯一的分享ID
        String shareId = UUID.randomUUID().toString(true).substring(0, 16);

        note.setIsShared(1);
        note.setShareId(shareId);
        baseMapper.updateById(note);

        return shareId;
    }

    @Override
    @Transactional
    public void unshareNote(Long userId, Long id) {
        Note note = baseMapper.selectById(id);
        if (note == null) {
            throw new BusinessException("笔记不存在");
        }
        if (!note.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作此笔记");
        }

        note.setIsShared(0);
        note.setShareId(null);
        baseMapper.updateById(note);
    }

    @Override
    public Note getSharedNote(String shareId) {
        if (StrUtil.isBlank(shareId)) {
            throw new BusinessException("分享链接无效");
        }

        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getShareId, shareId);
        wrapper.eq(Note::getIsShared, 1);

        Note note = baseMapper.selectOne(wrapper);
        if (note == null) {
            throw new BusinessException("分享链接不存在或已失效");
        }

        // 加载标签
        loadTagsForNote(note);

        return note;
    }

    private String generatePreview(String content) {
        if (StrUtil.isBlank(content)) {
            return "";
        }
        String text = content.replaceAll("<[^>]+>", "").replaceAll("\\s+", " ");
        return StrUtil.maxLength(text, 100);
    }

    private void saveTags(Long userId, Long noteId, List<String> tagNames) {
        for (String tagName : tagNames) {
            // 查找或创建标签
            Tag tag = tagMapper.selectOne(new LambdaQueryWrapper<Tag>()
                    .eq(Tag::getUserId, userId)
                    .eq(Tag::getName, tagName));

            if (tag == null) {
                tag = new Tag();
                tag.setUserId(userId);
                tag.setName(tagName);
                tagMapper.insert(tag);
            }

            // 创建笔记标签关联
            NoteTag noteTag = new NoteTag();
            noteTag.setNoteId(noteId);
            noteTag.setTagId(tag.getId());
            noteTagMapper.insert(noteTag);
        }
    }

    private void loadTagsForNote(Note note) {
        List<NoteTag> noteTags = noteTagMapper.selectList(new LambdaQueryWrapper<NoteTag>()
                .eq(NoteTag::getNoteId, note.getId()));
        if (!noteTags.isEmpty()) {
            List<Long> tagIds = noteTags.stream()
                    .map(NoteTag::getTagId)
                    .collect(Collectors.toList());
            List<Tag> tags = tagMapper.selectList(new LambdaQueryWrapper<Tag>()
                    .in(Tag::getId, tagIds));
            note.setTags(tags.stream()
                    .map(Tag::getName)
                    .collect(Collectors.toList()));
        } else {
            note.setTags(new ArrayList<>());
        }
    }
}
