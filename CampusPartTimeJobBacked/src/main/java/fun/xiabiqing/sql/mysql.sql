-- auto-generated definition
create table user
(
    id            bigint auto_increment comment '主键ID，自增'
        primary key,
    username      varchar(1024) default '默认用户名'                                                              null comment '用户名',
    image         varchar(1024) default 'https://web-tails5.oss-cn-beijing.aliyuncs.com/CampusPartJobDefault.png' null comment '头像',
    user_account  varchar(512)                                                                                    not null comment '账号',
    user_password varchar(512)                                                                                    not null comment '密码',
    role          tinyint       default 0                                                                         not null comment ' 角色 0用户 1商家 2管理员',
    creat_time    datetime      default CURRENT_TIMESTAMP                                                         not null comment '创建时间',
    update_time   datetime      default CURRENT_TIMESTAMP                                                         not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete     tinyint       default 0                                                                         not null comment '逻辑删除'
)
    comment '用户表' engine = InnoDB;

-- auto-generated definition
create table user_student
(
    id                bigint auto_increment comment '主键ID，自增'
        primary key,
    user_id_stu       bigint        not null comment '外键',
    age               int           null comment '年龄',
    gender            tinyint       null comment '性别 0男 1女',
    major             varchar(512)  null comment '专业',
    email             varchar(512)  null comment '邮箱',
    self_introduction varchar(3072) null comment '自我介绍',
    constraint user_id_stu
        foreign key (user_id_stu) references user (id)
)
    comment '学生表' engine = InnoDB;

-- auto-generated definition
create table user_business
(
    id          bigint auto_increment comment '主键ID，自增'
        primary key,
    user_id_bus bigint                                      not null comment '外键',
    position    varchar(512)                                null comment '职位',
    location    varchar(512)                                null comment '位置',
    company     varchar(512) default '商家还未填写公司名称' not null comment '公司',
    constraint user_id_bus
        foreign key (user_id_bus) references user (id)
)
    comment '商家' engine = InnoDB;

-- auto-generated definition
create table role_application
(
    id          bigint auto_increment
        primary key,
    stu_id      bigint                             not null comment '默认用户id',
    admin_id    bigint                             not null comment '管理员id',
    role        tinyint  default 1                 not null comment '申请的角色',
    information varchar(3072)                      not null comment '申请信息',
    image       varchar(1024)                      null comment '申请资政',
    status      tinyint  default 0                 not null comment '状态',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null comment '更新时间',
    is_delete   tinyint  default 0                 not null comment '逻辑删除',
    constraint admin_id_roleapp
        foreign key (admin_id) references admin (id),
    constraint stu_id_roleapp
        foreign key (stu_id) references user (id)
)
    comment '角色申请表' engine = InnoDB;

-- auto-generated definition
create table recruitment
(
    id           bigint auto_increment comment '主键ID，自增'
        primary key,
    bus_id_recru bigint                             not null comment '外键',
    title        varchar(512)                       not null comment '标题',
    salary       bigint                             not null comment '薪水',
    company      varchar(512)                       not null comment '公司',
    count        int                                not null comment '招聘人数',
    tags         varchar(1024)                      null comment '标签',
    job_details  varchar(3072)                      not null comment '岗位描述',
    create_time  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time  datetime default CURRENT_TIMESTAMP not null comment '更新时间',
    is_delete    tinyint  default 0                 not null comment '逻辑删除',
    constraint bus_id_recru
        foreign key (bus_id_recru) references user (id)
)
    comment '招聘表' engine = InnoDB;

-- auto-generated definition
create table job_application
(
    id          bigint auto_increment comment '主键ID，自增'
        primary key,
    stu_id      bigint                             not null,
    recru_id    bigint                             not null,
    information varchar(3072)                      null comment '申请信息',
    status      tinyint  default 0                 not null comment '申请状态 0正在申请中 1申请成功 2申请失败',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null comment '更新时间',
    is_delete   tinyint  default 0                 not null comment '逻辑删除',
    constraint recru_id_applica
        foreign key (recru_id) references recruitment (id),
    constraint stu_id_applica
        foreign key (stu_id) references user (id)
)
    comment '申请表' engine = InnoDB;

-- auto-generated definition
create table admin
(
    id            bigint auto_increment comment '主键ID，自增'
        primary key,
    username      varchar(1024) default '用户名为空'                                                            null comment '用户名',
    image         varchar(1024) default 'https://web-tails5.oss-cn-beijing.aliyuncs.com/CampusPartJobAdmin.png' null comment '头像',
    user_account  varchar(512)                                                                                  not null comment '账号',
    user_password varchar(512)                                                                                  not null comment '密码',
    role          tinyint       default 2                                                                       not null comment ' 角色 0用户 1商家 2管理员',
    creat_time    datetime      default CURRENT_TIMESTAMP                                                       not null comment '创建时间',
    update_time   datetime      default CURRENT_TIMESTAMP                                                       not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete     tinyint       default 0                                                                       not null comment '逻辑删除'
)
    comment '用户表' engine = InnoDB;
-- auto-generated definition
create table chat_message
(
    id              bigint auto_increment
        primary key,
    send_user_id    bigint                             not null comment '发送方用户ID',
    send_role       tinyint                            not null comment '发送方角色：0-学生，1-商家',
    receive_user_id bigint                             not null comment '接收方用户ID',
    receive_role    tinyint                            not null comment '接收方角色：0-学生，1-商家',
    msg_content     varchar(2000)                      not null comment '消息内容',
    create_time     datetime default CURRENT_TIMESTAMP not null comment '消息创建时间，自动填充',
    is_delete       tinyint  default 0                 not null comment '逻辑删除标记：0-未删除，1-已删除'
)
    comment '聊天消息记录表' engine = InnoDB;

-- auto-generated definition
create table tag
(
    id          bigint auto_increment comment '主键ID，自增'
        primary key,
    name        varchar(512)                       not null comment '名称',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    is_delete   tinyint  default 0                 not null comment '逻辑删除'
)
    comment '标签' engine = InnoDB;



