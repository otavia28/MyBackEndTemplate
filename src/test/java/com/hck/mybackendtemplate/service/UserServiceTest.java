package com.hck.mybackendtemplate.service;

import com.hck.mybackendtemplate.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Resource
    private UserService userService;

    /**
     * 测试 UserService 接口
     */
    @Test
    public void testAddUser() {
        User user = new User();
        user.setUsername("raisu");
        user.setUserAccount("123");
        user.setAvatarUrl("https://ts3.cn.mm.bing.net/th?id=ODLS.b689dae1-edfb-4adf-aad6-fbef599f8e14&w=32&h=32&qlt=90&pcl=fffffa&o=6&pid=1.2");
        user.setGender(0);
        user.setUserPassword("abcabc");
        user.setPhone("123@163.com");
        user.setEmail("13211111111");

        boolean result = userService.save(user);
        System.out.println(user.getId());
        Assertions.assertTrue(result);
    }

    /**
     * 测试 UserServiceImpl 中的用户注册功能
     */
    @Test
    void userRegister() {
        String userAccount = "hck";
        String userPassword = "12345678";
        String checkPassword = "12345678";

        // 账号长度不够
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, result);

        // 账号有非法字符
        userAccount = "h ck";
        result = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, result);

        // 密码和校验密码长度不够
        userAccount = "huchenkun";
        userPassword = "123456";
        checkPassword = "123456";
        result = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, result);

        // 密码和校验密码不相等
        userPassword = "12345678";
        checkPassword = "12345679";
        result = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, result);

        // 三者有一为空
        checkPassword = "";
        result = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, result);

        // 注册成功
        checkPassword = "12345678";
        result = userService.userRegister(userAccount, userPassword, checkPassword);

        // 账号不能重复
        result = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, result);
    }


}
