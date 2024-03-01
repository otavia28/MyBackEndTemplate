package com.hck.mybackendtemplate.service.impl;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.hck.mybackendtemplate.Mapper.UserMapper;
import com.hck.mybackendtemplate.common.ErrorCode;
import com.hck.mybackendtemplate.exception.BusinessException;
import com.hck.mybackendtemplate.model.domain.User;
import com.hck.mybackendtemplate.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hck.mybackendtemplate.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author huchenkun
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2024-02-05 16:28:20
*/

// 表示这是一个服务类，用于业务逻辑的实现。
@Service
// Lombok 提供的一种简化日志的注解
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    // @Resource 注解标注在成员变量上，表示需要注入一个 UserMapper 对象，Spring 框架会根据类型从容器中查找一个 UserMapper 对象并注入进来。
    @Resource
    // Mapper 文件夹专门用来从数据库中查询及修改数据，userMapper 对象可以将数据库表映射为 Java 对象，同时提供方法执行常见的数据库操作。
    private UserMapper userMapper;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "raisu";

    /**
     * 用户注册方法实现
     * @param userAccount 账号
     * @param userPassword 密码
     * @param checkPassword 验证密码
     * @return 新用户 id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {

        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码过短");
        }

        // 账号不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\\\\\[\\\\\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？\\s]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号存在特殊字符");
        }

        // 密码和校验密码要相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码输入不相同");
        }

        // 账号不能重复，由于这一步需要查询数据库，因此放到最后一次校验，节省资源占用
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();     // QueryWrapper 是用于封装查询条件的工具类。
        queryWrapper.eq("userAccount", userAccount);        // 借助查询工具类的 eq 方法添加查询条件。
        long count = userMapper.selectCount(queryWrapper);          // userMapper 的 selectCount 方法表示按照该对象中指定的条件进行查询。
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }

        // 2. 加密，使用 Spring 的工具类 DigestUtils

        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());  // 使用 MD5 对密码进行加密。

        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "注册失败");
        }
        return user.getId();
    }


    /**
     * 用户登录方法实现
     * @param userAccount 账号
     * @param userPassword 密码
     * @param request 请求
     * @return 脱敏后的用户信息
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号过短");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码过短");
        }

        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号存在特殊字符");
        }

        // 2. 加密，使用 Spring 的工具类 DigestUtils
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);
        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }


    /**
     * 用户脱敏方法实现
     * @param originUser 原始用户信息
     * @return 脱敏后的用户信息
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();

        // 把用户密码、用户更新时间、用户账户是否删除脱敏掉
        safetyUser.setId(originUser.getId());
        safetyUser.setUserName(originUser.getUserName());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());

        return safetyUser;
    }

    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态信息
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }
}




