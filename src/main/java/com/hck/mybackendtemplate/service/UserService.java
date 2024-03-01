package com.hck.mybackendtemplate.service;

import com.hck.mybackendtemplate.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author huchenkun
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2024-02-05 16:28:20
*/
public interface UserService extends IService<User> {
    /**
     * 用户注册接口
     * @param userAccount 账号
     * @param userPassword 密码
     * @param checkPassword 验证密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录接口
     * @param userAccount 账号
     * @param userPassword 密码
     * @param request 请求
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏接口
     * @param originUser 原始用户信息
     * @return 脱敏后的用户信息
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销接口
     * @param request
     * @return 注销是否成功
     */
    int userLogout(HttpServletRequest request);
}

