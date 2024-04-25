show databases;

create database 数据库名;
use 数据库名;

# 用户表 user
DROP TABLE IF EXISTS `user`;
create table user
(
    id           bigint auto_increment comment 'id' primary key,
    userName     varchar(256)              null comment '昵称',
    userAccount  varchar(256)              null comment '账号',
    userAvatar    varchar(1024)             null comment '头像',
    gender       tinyint                   null comment '性别', -- 女为 0，男为 1，未知为 2
    userPassword varchar(512)              not null comment '密码',
    phone        varchar(128)              null comment '电话',
    email        varchar(512)              null comment '邮箱',
    userStatus   int default 0             not null comment '用户状态', -- 正常为 0，异常为 1
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint default 0         not null comment '是否删除', -- 没有删除为 0，已经删除为 1
    userRole     int default 0             not null comment '用户身份'  -- 普通用户为 0，管理员为 1
) comment '用户表';
