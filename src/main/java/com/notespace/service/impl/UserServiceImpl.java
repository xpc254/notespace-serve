package com.notespace.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.notespace.entity.User;
import com.notespace.exception.BusinessException;
import com.notespace.mapper.UserMapper;
import com.notespace.service.UserService;
import com.notespace.utils.JwtUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private JwtUtils jwtUtils;

    @Override
    public Map<String, Object> register(String username, String email, String password) {
        if (StrUtil.isBlank(username) || StrUtil.isBlank(email) || StrUtil.isBlank(password)) {
            throw new BusinessException("用户名、邮箱或密码不能为空");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setStatus(1);

        baseMapper.insert(user);

        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getEmail());

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("email", user.getEmail());
        result.put("token", accessToken);
        result.put("refreshToken", refreshToken);

        return result;
    }

    @Override
    public Map<String, Object> login(String email, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        User user = baseMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException("邮箱或密码错误");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("邮箱或密码错误");
        }

        String accessToken = jwtUtils.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getEmail());

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("email", user.getEmail());
        result.put("avatar", user.getAvatar());
        result.put("token", accessToken);
        result.put("refreshToken", refreshToken);

        return result;
    }

    @Override
    public User getUserInfo(Long userId) {
        User user = baseMapper.selectById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    @Override
    public String refreshToken(String refreshToken) {
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new BusinessException("Refresh Token 无效或已过期");
        }

        if (!jwtUtils.isRefreshToken(refreshToken)) {
            throw new BusinessException("Token 类型错误");
        }

        Long userId = jwtUtils.getUserIdFromToken(refreshToken);
        User user = baseMapper.selectById(userId);

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        return jwtUtils.generateAccessToken(user.getId(), user.getEmail());
    }
}
