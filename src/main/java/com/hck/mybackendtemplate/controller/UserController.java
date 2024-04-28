package com.hck.mybackendtemplate.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hck.mybackendtemplate.common.BaseResponse;
import com.hck.mybackendtemplate.common.ErrorCode;
import com.hck.mybackendtemplate.common.ResultUtils;
import com.hck.mybackendtemplate.exception.BusinessException;
import com.hck.mybackendtemplate.model.domain.User;
import com.hck.mybackendtemplate.model.domain.request.UserLoginRequest;
import com.hck.mybackendtemplate.model.domain.request.UserRegisterRequest;
import com.hck.mybackendtemplate.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.Random;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import static com.hck.mybackendtemplate.constant.UserConstant.USER_LOGIN_STATE;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    // 在类中定义一个布隆过滤器
    private BloomFilter<String> bloomFilter = BloomFilter.create(
            Funnels.stringFunnel(StandardCharsets.UTF_8),
            10000, // 预计存放的最大元素数量
            0.01); // 误判率

    /**
     * 用户注册请求
     * @param userRegisterRequest 用户注册请求
     * @return 新用户 id
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);

        // 将注册成功的用户账号添加到布隆过滤器中
        bloomFilter.put(userAccount);

        return ResultUtils.success(result);
    }

    /**
     * 用户登录请求
     * @param userLoginRequest 用户登录请求
     * @param request 请求
     * @return 脱敏后的用户信息
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 使用布隆过滤器快速判断用户账号是否存在于系统中
        if (!bloomFilter.mightContain(userAccount)) {
            throw new BusinessException(ErrorCode.NOT_IN_BLOOM); // 用户账号不存在，返回请求参数错误
        }

        RLock lock = redissonClient.getLock("mybackendtemplate:user:login:lock");
        try {
            //只有1个线程能获取锁
            if (lock.tryLock(0,30000L, TimeUnit.MILLISECONDS)) {
                String redisKey = String.format("mybackendtemplate:user:login:%s", userAccount);
                // 尝试从 redis 中获取用户信息
                User cachedUser = (User) redisTemplate.opsForValue().get(redisKey);
                if (cachedUser != null) {
                    // 如果缓存中存在用户信息
                    return ResultUtils.success(cachedUser);
                } else {
                    // 如果没有，查询数据库
                    User user = userService.userLogin(userAccount, userPassword, request);
                    if (user != null) {
                        // 将查询到的用户信息存入 Redis，设置过期时间
                        Random random = new Random();
                        int minSeconds = 1500;
                        int maxSeconds = 1800;
                        int randomSeconds = random.nextInt(maxSeconds - minSeconds + 1) + minSeconds;
                        redisTemplate.opsForValue().set(redisKey, user, Duration.ofSeconds(randomSeconds)); // 设置随机过期时间
                        return ResultUtils.success(user);
                    } else {
                        throw new BusinessException(ErrorCode.PARAMS_ERROR);
                    }
                }
            } else {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        } catch (InterruptedException e) {
            // 捕获异常，打印到控制台
            e.printStackTrace();
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }

    /**
     * 用户查询请求（by userName）（管理员）
     * @param userName 用户名
     * @param request 请求
     * @return 用户列表
     */
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String userName, HttpServletRequest request) {
        // 判断是否为管理员
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(userName)) {
            queryWrapper.like("userName", userName);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

    /**
     * 用户删除请求（by id）（管理员）
     * @param id 用户 id
     * @param request 请求
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        // 判断是否为管理员
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 获取当前登录用户请求
     * @param request 请求
     * @return 脱敏后的用户信息
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();
        User user = userService.getById(userId);
        User safeUser = userService.getSafetyUser(user);
        return ResultUtils.success(safeUser);
    }

    /**
     * 用户注销（退出登录）请求
     * @param request 请求
     * @return 注销是否成功
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 管理员修改用户（管理员）
     * @param id 被修改用户 id
     * @param user 用户新信息
     * @param request 请求
     * @return 修改是否成功
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@PathVariable long id, @RequestBody User user, HttpServletRequest request) {
        //仅管理员可修改
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.updateById(user);

        // 如果布隆过滤器中不存在该用户账号，添加
        if (!bloomFilter.mightContain(user.getUserAccount())) {
            bloomFilter.put(user.getUserAccount());
        }

        return ResultUtils.success(b);
    }

    /**
     * 用户自己修改
     * @param user 用户新信息
     * @param request 请求
     * @return 修改是否成功
     */
    @PutMapping("/update")
    private BaseResponse<Boolean> updateCurrentUser(@RequestBody User user, HttpServletRequest request){
        User loginUser = (User)request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Long id = loginUser.getId();
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        user.setId(id);
        boolean b = userService.updateById(user);

        // 如果布隆过滤器中不存在该用户账号，添加
        if (!bloomFilter.mightContain(user.getUserAccount())) {
            bloomFilter.put(user.getUserAccount());
        }

        return ResultUtils.success(true);
    }

    /**
     * 添加用户请求（管理员）
     * @param user 新用户信息
     * @param request 请求
     * @return 新用户 id
     */
    @PostMapping("/add")
    public BaseResponse<Long> addUser(@RequestBody User user, HttpServletRequest request){
        //仅管理员可添加用户
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (StringUtils.isAnyBlank(user.getUserAccount(),user.getUserPassword())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long result = userService.userAdd(user);

        // 将添加成功的用户账号添加到布隆过滤器中
        bloomFilter.put(user.getUserAccount());

        return ResultUtils.success(result);
    }

    /**
     * 返回用户信息列表
     * @param request 请求
     * @return 用户信息列表
     */
    @GetMapping("/list")
    private BaseResponse<List<User>> listUsers(HttpServletRequest request){
        // 仅管理员可查询
        if (!userService.isAdmin(request)) {
            throw  new BusinessException(ErrorCode.NO_AUTH);
        }
        List<User> list = userService.list();
        // 用户信息脱敏
        List<User> safetyList = list.stream().map(user -> {
            return userService.getSafetyUser(user);
        }).collect(Collectors.toList());
        return ResultUtils.success(safetyList);
    }

}
