package com.notespace.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.notespace.common.PageResult;
import com.notespace.common.Result;
import com.notespace.entity.Note;
import com.notespace.service.NoteService;
import com.notespace.utils.JwtUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/note")
public class NoteController {

    @Resource
    private NoteService noteService;

    @Resource
    private JwtUtils jwtUtils;

    /**
     * 获取笔记列表
     * GET /api/note/list?notebookId={notebookId}&page={page}&size={size}
     */
    @GetMapping("/list")
    public Result<PageResult<Note>> getNoteList(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(required = false) Long notebookId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtils.getUserIdFromToken(token);
        IPage<Note> notePage = noteService.getNoteList(userId, notebookId, page, size);
        return Result.success(PageResult.of(notePage));
    }

    /**
     * 获取笔记详情
     * GET /api/note/detail?id={id}
     */
    @GetMapping("/detail")
    public Result<Note> getNoteDetail(
            @RequestHeader("Authorization") String authorization,
            @RequestParam Long id) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtils.getUserIdFromToken(token);
        Note note = noteService.getNoteDetail(userId, id);
        return Result.success(note);
    }

    /**
     * 创建笔记
     * POST /api/note/create
     */
    @PostMapping("/create")
    public Result<Note> createNote(
            @RequestHeader("Authorization") String authorization,
            @RequestBody Map<String, Object> request) {
        String token = authorization.replace("Bearer ", "");
        Long notebookId = Long.parseLong(request.get("notebookId").toString());
        String title = (String) request.get("title");
        String content = (String) request.get("content");
        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) request.get("tags");

        Long userId = jwtUtils.getUserIdFromToken(token);
        Note note = noteService.createNote(userId, notebookId, title, content, tags);
        return Result.success("创建成功", note);
    }

    /**
     * 更新笔记
     * POST /api/note/update
     */
    @PostMapping("/update")
    public Result<String> updateNote(
            @RequestHeader("Authorization") String authorization,
            @RequestBody Map<String, Object> request) {
        String token = authorization.replace("Bearer ", "");
        Long id = Long.parseLong(request.get("id").toString());
        String title = (String) request.get("title");
        String content = (String) request.get("content");
        @SuppressWarnings("unchecked")
        List<String> tags = (List<String>) request.get("tags");

        Long userId = jwtUtils.getUserIdFromToken(token);
        noteService.updateNote(userId, id, title, content, tags);
        return Result.success("更新成功");
    }

    /**
     * 删除笔记
     * POST /api/note/delete
     */
    @PostMapping("/delete")
    public Result<String> deleteNote(
            @RequestHeader("Authorization") String authorization,
            @RequestBody Map<String, String> request) {
        String token = authorization.replace("Bearer ", "");
        Long id = Long.parseLong(request.get("id"));

        Long userId = jwtUtils.getUserIdFromToken(token);
        noteService.deleteNote(userId, id);
        return Result.success("删除成功");
    }

    /**
     * 切换笔记固定状态
     * POST /api/note/togglePin
     */
    @PostMapping("/togglePin")
    public Result<Map<String, Object>> togglePin(
            @RequestHeader("Authorization") String authorization,
            @RequestBody Map<String, String> request) {
        String token = authorization.replace("Bearer ", "");
        Long id = Long.parseLong(request.get("id"));

        Long userId = jwtUtils.getUserIdFromToken(token);
        noteService.togglePin(userId, id);

        Note note = noteService.getById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("isPinned", note.getIsPinned() == 1);
        return Result.success("操作成功", result);
    }

    /**
     * 分享笔记 - 生成分享链接
     * POST /api/note/share
     */
    @PostMapping("/share")
    public Result<Map<String, String>> shareNote(
            @RequestHeader("Authorization") String authorization,
            @RequestBody Map<String, String> request) {
        String token = authorization.replace("Bearer ", "");
        Long id = Long.parseLong(request.get("id"));

        Long userId = jwtUtils.getUserIdFromToken(token);
        String shareId = noteService.shareNote(userId, id);

        Map<String, String> result = new HashMap<>();
        result.put("shareId", shareId);
        result.put("shareUrl", "/share/" + shareId);

        return Result.success("分享成功", result);
    }

    /**
     * 取消分享
     * POST /api/note/unshare
     */
    @PostMapping("/unshare")
    public Result<String> unshareNote(
            @RequestHeader("Authorization") String authorization,
            @RequestBody Map<String, String> request) {
        String token = authorization.replace("Bearer ", "");
        Long id = Long.parseLong(request.get("id"));

        Long userId = jwtUtils.getUserIdFromToken(token);
        noteService.unshareNote(userId, id);

        return Result.success("已取消分享");
    }

    /**
     * 获取分享的笔记（公开访问，无需认证）
     * GET /api/note/shared/{shareId}
     */
    @GetMapping("/shared/{shareId}")
    public Result<Note> getSharedNote(@PathVariable String shareId) {
        Note note = noteService.getSharedNote(shareId);
        return Result.success(note);
    }
}
