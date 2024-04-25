package com.hck.mybackendtemplate.service;

import com.hck.mybackendtemplate.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author huchenkun
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2024-04-24 17:44:00
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册接口
     * @param userAccount 账号
     * @param userPassword 密码
     * @param checkPassword 确认密码
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
     * 鉴权接口 1
     * @param request 请求
     * @return 是否为管理员
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 鉴权接口 2
     * @param loginUser 登录用户
     * @return 是否为管理员
     */
    boolean isAdmin(User loginUser);

    /**
     * 用户注销接口
     * @param request 请求
     * @return 注销是否成功
     */
    int userLogout(HttpServletRequest request);

    /**
     * 添加用户接口（管理员）
     * @param user 新用户信息
     * @return 新用户 id
     */
    long userAdd(User user);

    // /**
    //  * 获取当前登录用户接口
    //  * @param request 请求
    //  * @return 当前登录用户信息
    //  */
    // User getCurrentUser(HttpServletRequest request);

    // /**
    //  * 用户更新接口
    //  * @param user 待更新用户
    //  * @param loginUser 当前登录用户
    //  * @return 受影响的行数
    //  */
    // int updateUser(User user, User loginUser);
}
