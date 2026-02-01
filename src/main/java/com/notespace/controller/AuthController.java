package com.notespace.controller;

import com.notespace.common.Result;
import com.notespace.entity.User;
import com.notespace.service.UserService;
import com.notespace.utils.JwtUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private UserService userService;

    @Resource
    private JwtUtils jwtUtils;

    @PostMapping("/register")
    public Result<Object> register(@RequestBody User user) {
        return Result.success("注册成功",
                userService.register(user.getUsername(), user.getEmail(), user.getPassword()));
    }

    @PostMapping("/login")
    public Result<Object> login(@RequestBody User user) {
        return Result.success("登录成功",
                userService.login(user.getEmail(), user.getPassword()));
    }

    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success("登出成功");
    }

    @GetMapping("/info")
    public Result<Object> getUserInfo(@RequestHeader("Authorization") String authorization) {
        String token = authorization.replace("Bearer ", "");
        Long userId = jwtUtils.getUserIdFromToken(token);
        User user = userService.getUserInfo(userId);

        return Result.success(user);
    }

    @PostMapping("/refresh")
    public Result<Map<String, String>> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        String newToken = userService.refreshToken(refreshToken);
        Map<String, String> result = new HashMap<>();
        result.put("token", newToken);
        return Result.success("刷新成功", result);
    }
}
