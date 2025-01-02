package com.awesome.yunpicturebackend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户查询日志表(UserSearchLog)实体类
 *
 * @author awesomeRHQ
 * @since 2024-12-31 19:48:51
 */
@TableName(value = "user_search_log")
@Data
public class UserSearchLog implements Serializable {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 搜索词
     */
    private String search_word;

    /**
     * 搜索是否成功
     */
    private Boolean is_successful;

    /**
     * 错误信息
     */
    private String err_msg;

    /**
     * 地址信息
     */
    private String ip_address;

    /**
     * 设备信息
     */
    private String device_info;

    /**
     * 用户id
     */
    private Long user_id;

    /**
     * 创建时间
     */
    private Date create_time;

    @TableField(exist = false)
    private static final long serialVersionUID = -71718067548831117L;

}
