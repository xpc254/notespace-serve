package com.notespace.controller;

import com.notespace.common.Result;
import com.notespace.entity.Tag;
import com.notespace.service.TagService;
import com.notespace.utils.JwtUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/tag")
public class TagController {

    @Resource
    private TagService tagService;

    @Resource
    private JwtUtils jwtUtils;

    /**
     * 获取所有标签
     * GET /api/tag/list
     */
    @GetMapping("/list")
    public Result<List<Tag>> getTagList(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtils.getUserIdFromToken(token);
        List<Tag> tags = tagService.getTagList(userId);
        return Result.success(tags);
    }

    /**
     * 创建标签
     * POST /api/tag/create
     */
    @PostMapping("/create")
    public Result<Tag> createTag(
            @RequestHeader("Authorization") String authorization,
            @RequestBody Map<String, String> request) {
        String token = authorization.replace("Bearer ", "");
        String name = request.get("name");

        Long userId = jwtUtils.getUserIdFromToken(token);
        Tag tag = tagService.createTag(userId, name);
        return Result.success("创建成功", tag);
    }

    /**
     * 删除标签
     * POST /api/tag/delete
     */
    @PostMapping("/delete")
    public Result<String> deleteTag(
            @RequestHeader("Authorization") String authorization,
            @RequestBody Map<String, String> request) {
        String token = authorization.replace("Bearer ", "");
        Long id = Long.parseLong(request.get("id"));

        Long userId = jwtUtils.getUserIdFromToken(token);
        tagService.deleteTag(userId, id);
        return Result.success("删除成功");
    }
}
