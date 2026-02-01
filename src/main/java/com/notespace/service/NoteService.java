package com.notespace.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.notespace.entity.Note;

import java.util.List;

public interface NoteService extends IService<Note> {

    IPage<Note> getNoteList(Long userId, Long notebookId, Integer page, Integer size);

    Note getNoteDetail(Long userId, Long id);

    Note createNote(Long userId, Long notebookId, String title, String content, List<String> tags);

    void updateNote(Long userId, Long id, String title, String content, List<String> tags);

    void deleteNote(Long userId, Long id);

    void togglePin(Long userId, Long id);

    IPage<Note> searchNotes(Long userId, String keyword, Integer page, Integer size);

    /**
     * 分享笔记 - 生成分享链接
     * @param userId 用户ID
     * @param id 笔记ID
     * @return 分享信息 {shareId, shareUrl}
     */
    String shareNote(Long userId, Long id);

    /**
     * 取消分享
     * @param userId 用户ID
     * @param id 笔记ID
     */
    void unshareNote(Long userId, Long id);

    /**
     * 根据分享ID获取笔记（公开访问）
     * @param shareId 分享ID
     * @return 笔记内容
     */
    Note getSharedNote(String shareId);
}
