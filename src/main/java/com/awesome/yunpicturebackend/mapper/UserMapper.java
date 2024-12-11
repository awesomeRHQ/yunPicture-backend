package com.awesome.yunpicturebackend.mapper;

import com.awesome.yunpicturebackend.model.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author awesome
* @description 针对表【user(用户表)】的数据库操作Mapper
* @createDate 2024-12-11 19:06:03
* @Entity generator.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




