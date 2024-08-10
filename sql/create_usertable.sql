show databases;

create database 数据库名;
use 数据库名;

# 用户表 user
DROP TABLE IF EXISTS `user`;
create table user
(
    user_id       bigint auto_increment comment 'id' primary key,
    user_name     varchar(256)              null comment '昵称',
    user_account  varchar(256)              null comment '账号',
    user_avatar   varchar(1024)             null comment '头像',
    gender         tinyint                   null comment '性别', -- 女为 0，男为 1，未知为 2
    user_password varchar(512)              not null comment '密码',
    phone        varchar(128)              null comment '电话',
    email        varchar(512)              null comment '邮箱',
    user_status   int default 0             not null comment '用户状态', -- 正常为 0，异常为 1
    create_time   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint default 0         not null comment '是否删除', -- 没有删除为 0，已经删除为 1
    user_role     int default 0             not null comment '用户身份'  -- 普通用户为 0，管理员为 1
) comment '用户表';
