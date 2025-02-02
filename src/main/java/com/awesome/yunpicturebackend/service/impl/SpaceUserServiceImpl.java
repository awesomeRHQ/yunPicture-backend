package com.awesome.yunpicturebackend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.awesome.yunpicturebackend.common.ResponseCode;
import com.awesome.yunpicturebackend.exception.BusinessException;
import com.awesome.yunpicturebackend.exception.ThrowUtil;
import com.awesome.yunpicturebackend.mapper.SpaceUserMapper;
import com.awesome.yunpicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.awesome.yunpicturebackend.model.dto.spaceuser.SpaceUserEditRequest;
import com.awesome.yunpicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.awesome.yunpicturebackend.model.entity.Space;
import com.awesome.yunpicturebackend.model.entity.SpaceUser;
import com.awesome.yunpicturebackend.model.entity.User;
import com.awesome.yunpicturebackend.model.vo.space.SpaceInfo;
import com.awesome.yunpicturebackend.model.vo.spaceuser.SpaceUserInfo;
import com.awesome.yunpicturebackend.model.vo.spaceuser.SpaceUserVO;
import com.awesome.yunpicturebackend.model.vo.user.UserVO;
import com.awesome.yunpicturebackend.service.SpaceService;
import com.awesome.yunpicturebackend.service.SpaceUserService;
import com.awesome.yunpicturebackend.service.UserService;
import com.awesome.yunpicturebackend.util.ValidateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 针对表【space_user(空间用户关联)】的服务Service实现类
 *
 * @author awesomeRHQ
 * @since 2025-01-29 14:49:28
 */
@Service("SpaceUserService")
public class SpaceUserServiceImpl extends ServiceImpl<SpaceUserMapper, SpaceUser> implements SpaceUserService {

    @Resource
    private SpaceService spaceService;

    @Resource
    private UserService userService;

    @Override
    public boolean addSpaceUser(SpaceUserAddRequest spaceUserAddRequest) {
        // 1.校验参数
        ThrowUtil.throwIf(spaceUserAddRequest == null, ResponseCode.PARAMS_ERROR);
        Long spaceId = spaceUserAddRequest.getSpaceId();
        Long userId = spaceUserAddRequest.getUserId();
        ThrowUtil.throwIf(ValidateUtil.isNullOrNotPositive(spaceId), ResponseCode.PARAMS_ERROR);
        ThrowUtil.throwIf(ValidateUtil.isNullOrNotPositive(userId), ResponseCode.PARAMS_ERROR);
        // 2.检查空间是否存在该角色
        boolean exists = this.lambdaQuery().eq(SpaceUser::getSpaceId, spaceId).eq(SpaceUser::getUserId, userId).exists();
        if (exists) {
            throw new BusinessException(ResponseCode.OPERATION_ERROR,"当前角色已存在");
        }
        // 3.插入数据
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserAddRequest, spaceUser);
        return this.save(spaceUser);
    }

    @Override
    public List<SpaceUser> getSpaceUserBySpaceId(Long spaceId) {
        if (ValidateUtil.isNullOrNotPositive(spaceId)) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        return this.lambdaQuery().eq(SpaceUser::getSpaceId, spaceId).list();
    }

    @Override
    public List<SpaceUserVO> getSpaceUserVOBySpaceId(Long spaceId) {
        if (ValidateUtil.isNullOrNotPositive(spaceId)) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        List<SpaceUser> spaceUserList = getSpaceUserBySpaceId(spaceId);
        if (spaceUserList.isEmpty()){
            return new ArrayList<>();
        }
        ArrayList<SpaceUserVO> spaceUserVOList = new ArrayList<>();
        spaceUserList.forEach(spaceUser -> {
            // 遍历空间角色列表，构造空间角色详细信息结点
            SpaceUserVO spaceUserVO = new SpaceUserVO();
            BeanUtils.copyProperties(spaceUser, spaceUserVO);
            // 根据spaceId获取spaceInfo
            SpaceInfo spaceInfo = spaceService.getSpaceInfoBySpaceId(spaceId);
            spaceUserVO.setSpaceInfo(spaceInfo);
            // 根据userId获取userVO
            UserVO userVO = userService.getUserVO(spaceUser.getUserId());
            spaceUserVO.setUserVO(userVO);
            // 根据userId获取userVO
            spaceUserVOList.add(spaceUserVO);
        });
        return spaceUserVOList;
    }

    @Override
    public List<SpaceUserInfo> getSpaceUserInfoByUserId(Long spaceId) {
        if (ValidateUtil.isNullOrNotPositive(spaceId)) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        List<SpaceUser> spaceUserList = getSpaceUserBySpaceId(spaceId);
        if (spaceUserList.isEmpty()){
            return new ArrayList<>();
        }
        ArrayList<SpaceUserInfo> spaceUserInfoList = new ArrayList<>();
        spaceUserList.forEach(spaceUser -> {
            // 遍历空间角色列表，构造空间角色详细信息结点
            SpaceUserInfo spaceUserInfo = new SpaceUserInfo();
            BeanUtils.copyProperties(spaceUser, spaceUserInfo);
            // 根据userId获取userVO
            UserVO userVO = userService.getUserVO(spaceUser.getUserId());
            spaceUserInfo.setUserVO(userVO);
            // 根据userId获取userVO
            spaceUserInfoList.add(spaceUserInfo);
        });
        return spaceUserInfoList;
    }

    @Override
    public List<Space> getJoinSpaceByUserId(Long userId) {
        // 1.参数校验
        if (ValidateUtil.isNullOrNotPositive(userId)) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        // 2.查询当前用户的空间用户数据
        List<SpaceUser> spaceUserList = this.lambdaQuery().eq(SpaceUser::getUserId, userId).list();
        if (spaceUserList.isEmpty()) {
            return new ArrayList<>();
        }
        // 3.获取当前用户的全部空间Id
        ArrayList<Long> spaceIdList = new ArrayList<>();
        spaceUserList.forEach(spaceUser -> {
            if (!ValidateUtil.isNullOrNotPositive(spaceUser.getSpaceId())) {
                spaceIdList.add(spaceUser.getSpaceId());
            }
        });
        // 4.根据空间Id获取全部的空间数据
        if (spaceIdList.isEmpty()) {
            return new ArrayList<>();
        }
        return spaceService.listByIds(spaceIdList);
    }

    @Override
    public String getSpaceUserRole(Long id) {
        if (ValidateUtil.isNullOrNotPositive(id)) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        return this.lambdaQuery()
                .eq(SpaceUser::getId,id)
                .select(SpaceUser::getSpaceRole)
                .getSqlSelect();
    }

    @Override
    public String getSpaceUserRole(Long spaceId, Long userId) {
        if (ValidateUtil.isNullOrNotPositive(spaceId) || ValidateUtil.isNullOrNotPositive(userId)) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        return this.lambdaQuery()
                .eq(SpaceUser::getSpaceId, spaceId)
                .eq(SpaceUser::getUserId, userId)
                .select(SpaceUser::getSpaceRole)
                .getSqlSelect();
    }

    @Override
    public void validateSpaceUser(Object object, boolean add) {
        // 1.校验参数
        ThrowUtil.throwIf(Objects.isNull(object), ResponseCode.PARAMS_ERROR);
        // 2.参数分类
        SpaceUserAddRequest spaceUserAddRequest = null;
        SpaceUserEditRequest spaceUserEditRequest = null;
        if (object instanceof SpaceUserAddRequest) {
            spaceUserAddRequest = (SpaceUserAddRequest) object;
        }else if (object instanceof SpaceUserEditRequest) {
            spaceUserEditRequest = (SpaceUserEditRequest) object;
        }else {
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        // 3.校验数据
        if (spaceUserAddRequest != null){
            Long spaceId = spaceUserAddRequest.getSpaceId();
            Long userId = spaceUserAddRequest.getUserId();
            String spaceRole = spaceUserAddRequest.getSpaceRole();
            ThrowUtil.throwIf(ValidateUtil.isNullOrNotPositive(spaceId), ResponseCode.PARAMS_ERROR,"目标空间不能为空");
            ThrowUtil.throwIf(ValidateUtil.isNullOrNotPositive(userId), ResponseCode.PARAMS_ERROR,"目标用户不能为空");
            ThrowUtil.throwIf(StrUtil.isBlank(spaceRole), ResponseCode.PARAMS_ERROR,"用户角色不能为空");
        }
        if (spaceUserEditRequest != null){
            Long id = spaceUserEditRequest.getId();
            String spaceRole = spaceUserEditRequest.getSpaceRole();
            ThrowUtil.throwIf(ValidateUtil.isNullOrNotPositive(id), ResponseCode.PARAMS_ERROR,"修改目标不能为空");
            ThrowUtil.throwIf(StrUtil.isBlank(spaceRole), ResponseCode.PARAMS_ERROR,"用户角色不能为空");
        }
    }

    @Override
    public QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest) {
        ThrowUtil.throwIf(Objects.isNull(spaceUserQueryRequest), ResponseCode.PARAMS_ERROR);
        Long id = spaceUserQueryRequest.getId();
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        String spaceRole = spaceUserQueryRequest.getSpaceRole();

        QueryWrapper<SpaceUser> spaceUserQueryWrapper = new QueryWrapper<>();
        spaceUserQueryWrapper.eq("id", id);
        spaceUserQueryWrapper.eq("spaceId", spaceId);
        spaceUserQueryWrapper.eq("userId", userId);
        spaceUserQueryWrapper.eq("spaceRole", spaceRole);

        return spaceUserQueryWrapper;
    }

}
