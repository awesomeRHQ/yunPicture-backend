package com.awesome.yunpicturebackend.service;

import com.awesome.yunpicturebackend.model.dto.space.SpaceAddRequest;
import com.awesome.yunpicturebackend.model.dto.space.SpaceQueryRequest;
import com.awesome.yunpicturebackend.model.entity.Space;
import com.awesome.yunpicturebackend.model.entity.SpaceUser;
import com.awesome.yunpicturebackend.model.entity.User;
import com.awesome.yunpicturebackend.model.vo.space.SpaceInfo;
import com.awesome.yunpicturebackend.model.vo.space.SpaceVO;
import com.awesome.yunpicturebackend.model.vo.spaceuser.SpaceUserVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 针对表【space(空间)】的服务Service
 *
 * @author awesomeRHQ
 * @since 2025-01-15 14:21:37
 */
public interface SpaceService extends IService<Space> {

    /**
     * 验证空间信息
     * @param space 空间
     * @param add 判断新增还是更新
     */
    void validateSpace(Space space, boolean add);

    /**
     * 根据空间级别自动填充空间限额数据
     * @param space 空间
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 删除空间
     * @param space 空间
     * @return
     */
    boolean deleteSpace(Space space);

    /**
     * 初始化私密空间
     * @param loginUser 登录用户
     * @return 空间对象
     */
    SpaceVO initPrivateSpace(User loginUser);

    /**
     * 创建空间
     * @param spaceAddRequest 创建空间请求对象
     * @param loginUser 登录用户
     * @return 空间对象
     */
    SpaceVO createSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 检查空间限额
     * @param spaceId 空间id
     */
    void checkSpaceQuota(Long spaceId);

    /**
     * 根据空间Id获取空间基本信息
     * @param spaceId 空间Id
     * @return 空间基本信息
     */
    SpaceInfo getSpaceInfoBySpaceId(Long spaceId);

    /**
     * 获取我（拥有）的空间
     * @param userId 用户Id
     * @return 用户空间列表
     */
    List<Space> listMyOwnSpace(Long userId);

    /**
     * 获取我（加入）的空间
     * @param userId 用户Id
     * @return 用户加入空间列表
     */
    List<Space> listMyJoinSpaceByUserId(Long userId);

    /**
     * 构造QueryWrapper
     * @param spaceQueryRequest 空间查询条件
     * @return
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

}
