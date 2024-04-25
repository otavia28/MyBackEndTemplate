package com.hck.mybackendtemplate.DataImport;

import com.hck.mybackendtemplate.Mapper.UserMapper;
import com.hck.mybackendtemplate.service.UserService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import org.springframework.util.StopWatch;
import com.hck.mybackendtemplate.model.domain.User;
import java.util.Date;
import java.util.concurrent.*;

/**
 * @author: shayu
 * @date: 2022/12/06
 * @ClassName: yupao-backend01
 * @Description:
 */
@Component
public class InsertInfoTest {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserService userService;

    /**
     * 循环插入用户 10000 条：3143 ms
     */
    // @Scheduled(initialDelay = 5000,fixedRate = Long.MAX_VALUE )
    public void doInsertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 10000;
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUserName("假沙鱼");
            user.setUserAccount("yusha");
            user.setUserAvatar("shanghai.myqcloud.com/shayu931/shayu.png");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("123456789108");
            user.setEmail("shayu-yusha@qq.com");
            user.setUserStatus(0);
            user.setCreateTime(new Date(2024, 3, 16, 16, 59, 49));
            user.setUpdateTime(new Date(2024, 3, 16, 16, 59, 49));
            user.setIsDelete(0);
            user.setUserRole(0);
            userMapper.insert(user);
        }
        stopWatch.stop();
        System.out.println(stopWatch.getLastTaskTimeMillis());
    }


    /**
     * 批量插入用户 10000 条：1536 ms
     */
    // @Scheduled(initialDelay = 5000,fixedRate = Long.MAX_VALUE )
    public void doBatchInsertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 10000;
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUserName("假沙鱼");
            user.setUserAccount("yusha");
            user.setUserAvatar("shanghai.myqcloud.com/shayu931/shayu.png");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("123456789108");
            user.setEmail("shayu-yusha@qq.com");
            user.setUserStatus(0);
            user.setCreateTime(new Date(2024, 3, 16, 16, 59, 49));
            user.setUpdateTime(new Date(2024, 3, 16, 16, 59, 49));
            user.setIsDelete(0);
            user.setUserRole(0);
            userList.add(user);
        }
        userService.saveBatch(userList, 100);
        stopWatch.stop();
        System.out.println(stopWatch.getLastTaskTimeMillis());
    }


    /**
     * 并发批量插入用户 10000 条（默认线程池）：641 ms
     */
    // @Scheduled(initialDelay = 5000,fixedRate = Long.MAX_VALUE )
    public void doConcurrencyInsertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 10000;
        // 分十组
        int j = 0;
        // 批量插入数据的大小
        int batchSize = 100;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        // i 要根据数据量和插入批量来计算需要循环的次数
        for (int i = 0; i < INSERT_NUM / batchSize; i++) {
            List<User> userList = new ArrayList<>();
            while (true) {
                j++;
                User user = new User();
                user.setUserName("假沙鱼");
                user.setUserAccount("yusha");
                user.setUserAvatar("shanghai.myqcloud.com/shayu931/shayu.png");
                user.setGender(0);
                user.setUserPassword("12345678");
                user.setPhone("123456789108");
                user.setEmail("shayu-yusha@qq.com");
                user.setUserStatus(0);
                user.setCreateTime(new Date(2024, 3, 16, 16, 59, 49));
                user.setUpdateTime(new Date(2024, 3, 16, 16, 59, 49));
                user.setIsDelete(0);
                user.setUserRole(0);
                userList.add(user);
                if (j % batchSize == 0) {
                    break;
                }
            }
            // 异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("ThreadName：" + Thread.currentThread().getName());
                userService.saveBatch(userList, batchSize);
            });
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();

        stopWatch.stop();
        System.out.println(stopWatch.getLastTaskTimeMillis());
    }


    private ExecutorService executorService = new ThreadPoolExecutor(
            20,
            1000,
            1000,
            TimeUnit.MINUTES,
            new ArrayBlockingQueue<>(10000)
    );

    /**
     * 并发批量插入用户 10000 条（自定义线程池）：636 ms
     */
    // @Scheduled(initialDelay = 5000,fixedRate = Long.MAX_VALUE )
    public void doSelfConcurrencyInsertUser() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 10000;
        // 分十组
        int j = 0;
        // 批量插入数据的大小
        int batchSize = 100;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        // i 要根据数据量和插入批量来计算需要循环的次数
        for (int i = 0; i < INSERT_NUM / batchSize; i++) {
            List<User> userList = new ArrayList<>();
            while (true) {
                j++;
                User user = new User();
                user.setUserName("假沙鱼");
                user.setUserAccount("yusha");
                user.setUserAvatar("shanghai.myqcloud.com/shayu931/shayu.png");
                user.setGender(0);
                user.setUserPassword("12345678");
                user.setPhone("123456789108");
                user.setEmail("shayu-yusha@qq.com");
                user.setUserStatus(0);
                user.setCreateTime(new Date(2024, 3, 16, 16, 59, 49));
                user.setUpdateTime(new Date(2024, 3, 16, 16, 59, 49));
                user.setIsDelete(0);
                user.setUserRole(0);
                userList.add(user);
                if (j % batchSize == 0) {
                    break;
                }
            }
            // 异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("ThreadName：" + Thread.currentThread().getName());
                userService.saveBatch(userList, batchSize);
            }, executorService);    // 自定义线程池
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();

        stopWatch.stop();
        System.out.println(stopWatch.getLastTaskTimeMillis());
    }

}
