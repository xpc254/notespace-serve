package com.notespace.controller;

import com.notespace.common.Result;
import com.notespace.entity.Notebook;
import com.notespace.service.NotebookService;
import com.notespace.utils.JwtUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notebook")
public class NotebookController {

    @Resource
    private NotebookService notebookService;

    @Resource
    private JwtUtils jwtUtils;

    /**
     * 获取所有笔记本
     * GET /api/notebook/list
     */
    @GetMapping("/list")
    public Result<List<Notebook>> getNotebookList(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtils.getUserIdFromToken(token);
        List<Notebook> notebooks = notebookService.getNotebookList(userId);
        return Result.success(notebooks);
    }

    /**
     * 创建笔记本
     * POST /api/notebook/create
     */
    @PostMapping("/create")
    public Result<Notebook> createNotebook(
            @RequestHeader("Authorization") String authorization,
            @RequestBody Map<String, String> request) {
        String token = authorization.replace("Bearer ", "");
        String name = request.get("name");
        String icon = request.getOrDefault("icon", "folder");
        String color = request.getOrDefault("color", "#3b82f6");

        Long userId = jwtUtils.getUserIdFromToken(token);
        Notebook notebook = notebookService.createNotebook(userId, name, icon, color);
        return Result.success("创建成功", notebook);
    }

    /**
     * 更新笔记本
     * POST /api/notebook/update
     */
    @PostMapping("/update")
    public Result<String> updateNotebook(
            @RequestHeader("Authorization") String authorization,
            @RequestBody Map<String, String> request) {
        String token = authorization.replace("Bearer ", "");
        Long id = Long.parseLong(request.get("id"));
        String name = request.get("name");
        String icon = request.get("icon");
        String color = request.get("color");

        Long userId = jwtUtils.getUserIdFromToken(token);
        notebookService.updateNotebook(userId, id, name, icon, color);
        return Result.success("更新成功");
    }

    /**
     * 删除笔记本
     * POST /api/notebook/delete
     */
    @PostMapping("/delete")
    public Result<String> deleteNotebook(
            @RequestHeader("Authorization") String authorization,
            @RequestBody Map<String, String> request) {
        String token = authorization.replace("Bearer ", "");
        Long id = Long.parseLong(request.get("id"));

        Long userId = jwtUtils.getUserIdFromToken(token);
        notebookService.deleteNotebook(userId, id);
        return Result.success("删除成功");
    }

    /**
     * 获取笔记本详情
     * GET /api/notebook/detail?id={id}
     */
    @GetMapping("/detail")
    public Result<Notebook> getNotebookDetail(
            @RequestHeader("Authorization") String authorization,
            @RequestParam Long id) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtils.getUserIdFromToken(token);
        Notebook notebook = notebookService.getById(id);
        if (notebook != null && !notebook.getUserId().equals(userId)) {
            return Result.error(403, "无权限查看此笔记本");
        }
        return Result.success(notebook);
    }
}
