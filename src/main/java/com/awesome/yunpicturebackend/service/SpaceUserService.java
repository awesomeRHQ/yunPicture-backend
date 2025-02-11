package com.awesome.yunpicturebackend.service;

import com.awesome.yunpicturebackend.common.DeleteRequest;
import com.awesome.yunpicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.awesome.yunpicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.awesome.yunpicturebackend.model.entity.Space;
import com.awesome.yunpicturebackend.model.entity.SpaceUser;
import com.awesome.yunpicturebackend.model.entity.User;
import com.awesome.yunpicturebackend.model.vo.spaceuser.SpaceUserInfo;
import com.awesome.yunpicturebackend.model.vo.spaceuser.SpaceUserVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 针对表【space_user(空间用户关联)】的服务Service
 *
 * @author awesomeRHQ
 * @since 2025-01-29 14:49:27
 */
public interface SpaceUserService extends IService<SpaceUser> {

    /**
     * 给空间添加成员
     * @param spaceUserAddRequest 空间成员添加请求类
     * @return
     */
    boolean addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    /**
     * 根据空间Id获取空间用户列表
     * @param spaceId 空间角色查询请求类
     * @return 空间用户列表
     */
    List<SpaceUser> listSpaceUserBySpaceId(Long spaceId);

    /**
     * 获取空间用户列表
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

    /**
     * 根据用户Id获取用户加入的空间
     * @param userId 用户Id
     * @return 已加入空间列表
     */
    List<Space> getJoinSpaceByUserId(Long userId);

    /**
     * 根据空间角色表Id获取用户角色
     * @param id 空间角色表Id
     * @return 用户角色
     */
    String getSpaceUserRole(Long id);

    /**
     * 根据空间Id和UserId获取用户角色
     * @param spaceId 空间Id
     * @param userId 用户Id
     * @return 用户角色
     */
    String getSpaceUserRole(Long spaceId, Long userId);

    /**
     * 验证空间成员数据的有效性
     * @param object 空间成员数据
     * @param add 是否为新增
     */
    void validateSpaceUser(Object object,boolean add);

    /**
     * 拼装查询Wrapper
     * @param spaceUserQueryRequest
     * @return
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

}
