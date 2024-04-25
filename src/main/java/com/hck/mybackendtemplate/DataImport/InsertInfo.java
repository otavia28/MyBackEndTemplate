package com.hck.mybackendtemplate.DataImport;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.read.listener.PageReadListener;
import com.hck.mybackendtemplate.model.domain.User;
import com.hck.mybackendtemplate.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


@Component
public class InsertInfo {

    @Resource
    private UserService userService;

    /**
     * 批量插入用户 1000000 条：
     * 每批插入 100000 条：145849 ms
     */
    // @Scheduled(initialDelay = 5000,fixedRate = Long.MAX_VALUE )
    public void doBatchInsertInfo() {
        // 写法1：JDK8+ ,不用额外写一个 DemoDataListener
        // since: 3.0.0-beta1
        String fileName = "/Users/huchenkun/Desktop/userInfo.xlsx";
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        List<User> userList = new ArrayList<>();
        // 监听器读取
        EasyExcel.read(fileName, ImportExcelTable.class, new PageReadListener<ImportExcelTable>(dataList -> {
            for (ImportExcelTable data : dataList) {
                User user = new User();
                user.setUserName(data.getUserName());
                user.setUserAccount(data.getUserAccount());
                user.setUserAvatar(data.getUserAvatar());
                user.setGender(data.getGender());
                user.setUserPassword(data.getUserPassword());
                user.setPhone(data.getPhone());
                user.setEmail(data.getEmail());
                user.setUserStatus(data.getUserStatus());
                user.setCreateTime(data.getCreateTime());
                user.setUpdateTime(data.getUpdateTime());
                user.setIsDelete(data.getIsDelete());
                user.setUserRole(data.getUserRole());
                userList.add(user);
            }
        })).sheet().doReadSync();
        userService.saveBatch(userList, 100000);
        stopWatch.stop();
        System.out.println(stopWatch.getLastTaskTimeMillis());
    }


    /**
     * 默认线程池（线程数为本机 CPU - 1 = 7）并发批量插入 1000000 条：54211 ms
     */
    // @Scheduled(initialDelay = 5000,fixedRate = Long.MAX_VALUE )
    public void doPoolBatchInsertInfo() {
        String fileName = "/Users/huchenkun/Desktop/userInfo.xlsx";
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        int batchSize = 100000;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        List<User> userList = new ArrayList<>();
        // 监听器读取
        EasyExcel.read(fileName, ImportExcelTable.class, new PageReadListener<ImportExcelTable>(dataList -> {
            for (ImportExcelTable data : dataList) {
                User user = new User();
                user.setUserName(data.getUserName());
                user.setUserAccount(data.getUserAccount());
                user.setUserAvatar(data.getUserAvatar());
                user.setGender(data.getGender());
                user.setUserPassword(data.getUserPassword());
                user.setPhone(data.getPhone());
                user.setEmail(data.getEmail());
                user.setUserStatus(data.getUserStatus());
                user.setCreateTime(data.getCreateTime());
                user.setUpdateTime(data.getUpdateTime());
                user.setIsDelete(data.getIsDelete());
                user.setUserRole(data.getUserRole());
                userList.add(user);
                if (userList.size() >= batchSize) {
                    List<User> batch = new ArrayList<>(userList);
                    userList.clear();
                    // 异步执行
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        System.out.println("ThreadName：" + Thread.currentThread().getName());
                        userService.saveBatch(batch, batchSize);
                    });
                    futureList.add(future);
                }

            }
        })).sheet().doReadSync();
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getLastTaskTimeMillis());
    }

    private ExecutorService executorService = new ThreadPoolExecutor(
            10,
            1000,
            1000,
            TimeUnit.MINUTES,
            new ArrayBlockingQueue<>(10000)
    );
    /**
     * 自定义线程池（线程数为 10 ）并发批量插入 1000000 条：54284 ms，没有默认线程池快
     */
    // @Scheduled(initialDelay = 5000,fixedRate = Long.MAX_VALUE )
    public void doSelfPoolBatchInsertInfo() {
        String fileName = "/Users/huchenkun/Desktop/userInfo.xlsx";
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        int batchSize = 100000;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        List<User> userList = new ArrayList<>();
        // 监听器读取
        EasyExcel.read(fileName, ImportExcelTable.class, new PageReadListener<ImportExcelTable>(dataList -> {
            for (ImportExcelTable data : dataList) {
                User user = new User();
                user.setUserName(data.getUserName());
                user.setUserAccount(data.getUserAccount());
                user.setUserAvatar(data.getUserAvatar());
                user.setGender(data.getGender());
                user.setUserPassword(data.getUserPassword());
                user.setPhone(data.getPhone());
                user.setEmail(data.getEmail());
                user.setUserStatus(data.getUserStatus());
                user.setCreateTime(data.getCreateTime());
                user.setUpdateTime(data.getUpdateTime());
                user.setIsDelete(data.getIsDelete());
                user.setUserRole(data.getUserRole());
                userList.add(user);
                if (userList.size() >= batchSize) {
                    List<User> batch = new ArrayList<>(userList);
                    userList.clear();
                    // 异步执行
                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        System.out.println("ThreadName：" + Thread.currentThread().getName());
                        userService.saveBatch(batch, batchSize);
                    }, executorService);    // 自定义线程池
                    futureList.add(future);
                }

            }
        })).sheet().doReadSync();
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getLastTaskTimeMillis());
    }

}
