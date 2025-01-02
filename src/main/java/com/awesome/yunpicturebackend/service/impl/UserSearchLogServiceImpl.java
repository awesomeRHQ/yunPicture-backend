package com.awesome.yunpicturebackend.service.impl;

import com.awesome.yunpicturebackend.mapper.UserSearchLogMapper;
import com.awesome.yunpicturebackend.model.entity.UserSearchLog;
import com.awesome.yunpicturebackend.service.UserSearchLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 针对表【user_search_log(用户查询日志表)】的服务Service实现类
 *
 * @author awesomeRHQ
 * @since 2024-12-31 19:49:34
 */
@Service("UserSearchLogService")
public class UserSearchLogServiceImpl extends ServiceImpl<UserSearchLogMapper, UserSearchLog> implements UserSearchLogService {


}
