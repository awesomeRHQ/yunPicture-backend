package com.awesome.yunpicturebackend.mapper;

import com.awesome.yunpicturebackend.model.entity.UserSearchLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 针对表【user_search_log(用户查询日志表)】的数据库操作Mapper
 *
 * @author awesomeRHQ
 * @since 2024-12-31 19:49:34
 */
@Mapper
public interface UserSearchLogMapper extends BaseMapper<UserSearchLog> {


}
