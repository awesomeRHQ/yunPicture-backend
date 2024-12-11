use yun_picture;
-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                               not null comment '用户账号',
    userPassword varchar(256)                               not null comment '用户密码',
    userName     varchar(64)                                null comment '用户名',
    userAvatar   varchar(1024)                              null comment '用户头像',
    userProfile  varchar(512)                               null comment '用户简介',
    userRole     varchar(128)   default 'user'              not null comment '用户角色',
    editTime     datetime       default CURRENT_TIMESTAMP   not null comment '编辑时间',
    createTime   datetime       default CURRENT_TIMESTAMP   not null comment '创建时间',
    updateTime   datetime       default CURRENT_TIMESTAMP   not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint        default 0                   not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
) comment '用户表' collate = utf8mb4_unicode_ci;

create table if not exists holiday
(
    id         bigint auto_increment comment 'id' primary key,
    hName      varchar(128)                       not null comment '节日名称',
    hDesc     text                               null comment '节日描述',
    hDate       date                               null comment '节日时间',
    hDateYear      int                                 null comment '节日时间，年',
    hDateMonth      int                                 null comment '节日时间，月',
    hDateDay      int                                 null comment '节日时间，日',
    hPicture  varchar(256)                           null comment '示例图片',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
    ) comment '节日表';

create table if not exists schedule
(
    id         bigint auto_increment comment 'id' primary key,
    title      varchar(128)                       not null comment '标题',
    remark     text                               null comment '备注',
    time       date                               null comment '日程时间',
    classId    bigint                             null comment '分类Id',
    isTopping  int  default 0                     null comment '是否置顶(1-是、0-否)',
    isHighlight  int  default 0                   null comment '是否突出显示(1-是、0-否)',
    endTime    datetime                           null comment '结束时间',
    scheduleType varchar(32)                      null comment '日程类型(temporary,anniversary)',
    createUserId     bigint                             not null comment '创建人Id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
) comment '纪念日';

create table if not exists schoolTerm
(
    id         bigint auto_increment comment 'id' primary key,
    title      varchar(128)                       not null comment '标题',
    startDate  date                                 null comment '学期开始时间',
    endDate    date                                 null comment '学期结束时间',
    createUserId     bigint                             not null comment '创建人Id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
    ) comment '学期表';

create table if not exists schoolTermWeek
(
    id         bigint auto_increment comment 'id' primary key,
    schoolTermId bigint                         not null comment '学期Id',
    weekIndex       int                                 null commet '周序号',
    dayCount    int                                  null commet '天数',
    startDate   date                                  null comment '周开始日期',
    endDate     date                                  null comment '周结束日期',
    includeDate  varchar(128)                                 null comment '周包含日期',
    createUserId     bigint                             not null comment '创建人Id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
    ) comment '学期周表';

create table if not exists course
(
    id              bigint auto_increment comment 'id' primary key,
    schoolTermId    bigint not null comment '学期Id',
    schoolTermWeekIds    varchar(64)    not null comment '学期周Id',
    courseName           varchar(128)                       not null comment '课程名称',
    teacherName           varchar(128)                        null comment '教师名称',
    classroom           varchar(128)                        null comment '教室名称',
    weekDayIndex           int                        null comment '星期数',
    courseIndex           varchar(16)                     null comment '课程节数（1-第一节课，12-第一二节课以此类推）',
    startTime       varchar(16)                           null comment '开始时间',
    endTime       varchar(16)                           null comment '结束时间',
    createUserId     bigint                             not null comment '创建人Id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除'
    ) comment '课程表';