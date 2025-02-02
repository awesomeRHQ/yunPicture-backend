package com.awesome.yunpicturebackend.mapper;

import com.awesome.yunpicturebackend.model.entity.SpaceUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 针对表【space_user(空间用户关联)】的数据库操作Mapper
 *
 * @author awesomeRHQ
 * @since 2025-01-29 14:49:28
 */
@Mapper
public interface SpaceUserMapper extends BaseMapper<SpaceUser> {


}
