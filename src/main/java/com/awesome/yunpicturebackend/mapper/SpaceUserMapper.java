package com.awesome.yunpicturebackend.mapper;

import com.awesome.yunpicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.awesome.yunpicturebackend.model.entity.SpaceUser;
import com.awesome.yunpicturebackend.model.vo.spaceuser.SpaceUserInfo;
import com.awesome.yunpicturebackend.model.vo.spaceuser.SpaceUserVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 针对表【space_user(空间用户关联)】的数据库操作Mapper
 *
 * @author awesomeRHQ
 * @since 2025-01-29 14:49:28
 */
@Mapper
public interface SpaceUserMapper extends BaseMapper<SpaceUser> {

    /**
     * 查询空间用户列表
     * @param spaceUserQueryRequest 空间角色查询请求类
     * @return 空间用户列表
     */
    List<SpaceUser> listSpaceUser(SpaceUserQueryRequest spaceUserQueryRequest);

    /**
     * 获取空间用户VO列表
     * @param spaceUserQueryRequest 空间角色查询请求类
     * @return 空间用户VO列表
     */
    List<SpaceUserVO> listSpaceUserVO(SpaceUserQueryRequest spaceUserQueryRequest);

    /**
     * 获取空间用户信息列表
     * @param spaceUserQueryRequest 空间角色查询请求类
     * @return 空间用户信息列表
     */
    List<SpaceUserInfo> listSpaceUserInfo(SpaceUserQueryRequest spaceUserQueryRequest);

}
