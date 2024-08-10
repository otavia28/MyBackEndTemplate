package com.hck.mybackendtemplate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hck.mybackendtemplate.common.ErrorCode;
import com.hck.mybackendtemplate.exception.BusinessException;
import com.hck.mybackendtemplate.model.domain.User;
import com.hck.mybackendtemplate.service.UserService;
import com.hck.mybackendtemplate.Mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hck.mybackendtemplate.constant.DefaultInfo.*;
import static com.hck.mybackendtemplate.constant.UserConstant.ADMIN_ROLE;
import static com.hck.mybackendtemplate.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author huchenkun
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2024-04-24 17:44:00
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    // @Resource 注解标注在成员变量上，表示需要注入一个 UserMapper 对象，Spring 框架会根据类型从容器中查找一个 UserMapper 对象并注入进来。
    @Resource
    // Mapper 文件夹专门用来从数据库中查询及修改数据，userMapper 对象可以将数据库表映射为 Java 对象，同时提供方法执行常见的数据库操作。
    private UserMapper userMapper;

    /**
     * 用户注册
     * @param userAccount 账号
     * @param userPassword 密码
     * @param checkPassword 确认密码
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
        // 设置默认用户名和头像，以便登录后可以退出
        user.setUserName(DEFAULT_USERNAME);
        user.setUserAvatar(DEFAULT_AVATAR);
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "注册失败");
        }
        return user.getUserId();
    }

    /**
     * 用户登录
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
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");
        }

        // 3. 用户脱敏
        User safetyUser = getSafetyUser(user);

        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    /**
     * 用户脱敏
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
        safetyUser.setUserId(originUser.getUserId());
        safetyUser.setUserName(originUser.getUserName());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setUserAvatar(originUser.getUserAvatar());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());

        return safetyUser;
    }

    /**
     * 鉴权是否为管理员 1
     * @param request 请求
     * @return 是否为管理员
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 鉴权是否为管理员 2
     * @param loginUser 登录用户
     * @return 是否为管理员
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 用户注销（退出登录）
     * @param request 请求
     * @return 注销是否成功
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态信息
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 添加新用户（管理员）
     * @param user 新用户信息
     * @return 新用户 id
     */
    @Override
    public long userAdd(User user) {

        // 1. 校验
        if(StringUtils.isAnyBlank(user.getUserAccount(),user.getUserPassword())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if(user.getUserAccount().length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号过短");

        }
        if(user.getUserPassword().length() < 8 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码过短");
        }

        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(user.getUserAccount());
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号存在特殊字符");
        }

        // 账号不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();     // QueryWrapper 是用于封装查询条件的工具类。
        queryWrapper.eq("userAccount", user.getUserAccount());        // 借助查询工具类的 eq 方法添加查询条件。
        long count = userMapper.selectCount(queryWrapper);          // userMapper 的 selectCount 方法表示按照该对象中指定的条件进行查询。
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }

        // 2. 设置默认密码
        user.setUserPassword(DEFAULT_USERPASSWORD);

        // 3. 对密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + user.getUserPassword()).getBytes());  // 使用 MD5 对密码进行加密。

        // 4. 插入数据
        user.setUserPassword(encryptPassword);
        boolean saveResult = this.save(user);
        if(!saveResult){
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "注册失败");
        }
        return user.getUserId();
    }

    // /**
    //  * 获取当前登录用户
    //  * @param request 请求
    //  * @return 当前登录用户信息
    //  */
    // @Override
    // public User getCurrentUser(HttpServletRequest request) {
    //     if (request == null) {
    //         return null;
    //     }
    //     Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
    //     if (userObj == null) {
    //         throw new BusinessException(ErrorCode.NOT_LOGIN);
    //     }
    //     return (User) userObj;
    // }

    // /**
    //  * 更新用户
    //  * @param user 待更新用户
    //  * @param loginUser 当前登录用户
    //  * @return 受影响的行数
    //  */
    // @Override
    // public int updateUser(User user, User loginUser) {
    //     long userId = user.getUserId();
    //     if (userId <= 0) {
    //         throw new BusinessException(ErrorCode.PARAMS_ERROR);
    //     }
    //     // 既不是管理员又想改别人的数据是不被允许的
    //     if (!isAdmin(loginUser) && userId != loginUser.getUserId()) {
    //         throw new BusinessException(ErrorCode.NO_AUTH);
    //     }
    //     User oldUser = userMapper.selectById(userId);
    //     if (oldUser == null) {
    //         throw new BusinessException(ErrorCode.NULL_ERROR);
    //     }
    //     return userMapper.updateById(user);
    // }
    //
}




