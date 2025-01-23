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

-- 图片表
create table if not exists picture
(
    id              bigint auto_increment comment 'id' primary key,
    url             varchar(512)                                not null comment '图片url',
    name            varchar(128)                                not null comment '图片名称',
    introduction    varchar(512)                                null comment '图片简介',
    category        varchar(64)                                 null comment '图片分类',
    tags            varchar(512)                                null comment '图片标签',
    picSize         bigint                                      not null comment '图片大小',
    picWidth        int                                         not null comment '图片宽度',
    picHeight       int                                         not null comment '图片高度',
    picScale        double                                      not null comment '图片宽高比',
    picFormat       varchar(32)                                 not null comment '图片格式',
    userId          bigint                                      not null comment '创建用户Id',
    editTime        datetime       default CURRENT_TIMESTAMP    not null comment '编辑时间',
    createTime      datetime       default CURRENT_TIMESTAMP    not null comment '创建时间',
    updateTime      datetime       default CURRENT_TIMESTAMP    not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete        tinyint        default 0                    not null comment '是否删除',
    INDEX idx_name (name),
    INDEX idx_introduction (introduction),
    INDEX idx_category (category),
    INDEX idx_tags (tags),
    INDEX idx_userId (userId)
) comment '图片表' collate = utf8mb4_unicode_ci;

-- 空间表
create table if not exists space
(
    id         bigint auto_increment comment 'id' primary key,
    spaceName  varchar(128)                       null comment '空间名称',
    spaceLevel int      default 0                 null comment '空间级别：0-普通版 1-专业版 2-旗舰版',
    maxSize    bigint   default 0                 null comment '空间图片的最大总大小',
    maxCount   bigint   default 0                 null comment '空间图片的最大数量',
    totalSize  bigint   default 0                 null comment '当前空间下图片的总大小',
    totalCount bigint   default 0                 null comment '当前空间下的图片数量',
    userId     bigint                             not null comment '创建用户 id',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime   datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    -- 索引设计
    index idx_userId (userId),        -- 提升基于用户的查询效率
    index idx_spaceName (spaceName),  -- 提升基于空间名称的查询效率
    index idx_spaceLevel (spaceLevel) -- 提升按空间级别查询的效率
) comment '空间' collate = utf8mb4_unicode_ci;
