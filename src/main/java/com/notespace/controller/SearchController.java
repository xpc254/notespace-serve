package com.notespace.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.notespace.common.PageResult;
import com.notespace.common.Result;
import com.notespace.entity.Note;
import com.notespace.service.NoteService;
import com.notespace.utils.JwtUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Resource
    private NoteService noteService;

    @Resource
    private JwtUtils jwtUtils;

    /**
     * 全文搜索笔记
     * GET /api/search/note?keyword={keyword}&page={page}&size={size}
     */
    @GetMapping("/note")
    public Result<Map<String, Object>> searchNotes(
            @RequestHeader("Authorization") String authorization,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtils.getUserIdFromToken(token);
        IPage<Note> notePage = noteService.searchNotes(userId, keyword, page, size);

        Map<String, Object> result = new HashMap<>();
        result.put("total", notePage.getTotal());
        result.put("keyword", keyword);
        result.put("list", notePage.getRecords());
        return Result.success(result);
    }

    /**
     * 搜索标签
     * GET /api/search/tag?keyword={keyword}
     */
    @GetMapping("/tag")
    public Result<List<Map<String, Object>>> searchTags(
            @RequestHeader("Authorization") String authorization,
            @RequestParam String keyword) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtils.getUserIdFromToken(token);
        // TODO: 实现标签搜索
        return Result.success(new ArrayList<>());
    }

    /**
     * 按标签筛选笔记
     * GET /api/note/byTag?tagId={tagId}&page={page}&size={size}
     */
    @GetMapping("/byTag")
    public Result<Map<String, Object>> getNotesByTag(
            @RequestHeader("Authorization") String authorization,
            @RequestParam Long tagId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        // TODO: 实现按标签筛选笔记
        return Result.success(new HashMap<>());
    }
}
