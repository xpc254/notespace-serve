package com.notespace.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.notespace.entity.User;

import java.util.Map;

public interface UserService extends IService<User> {

    Map<String, Object> register(String username, String email, String password);

    Map<String, Object> login(String email, String password);

    User getUserInfo(Long userId);

    String refreshToken(String refreshToken);
}
