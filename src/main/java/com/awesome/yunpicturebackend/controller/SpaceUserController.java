package com.awesome.yunpicturebackend.controller;

import com.awesome.yunpicturebackend.common.BaseResponse;
import com.awesome.yunpicturebackend.common.DeleteRequest;
import com.awesome.yunpicturebackend.common.ResponseCode;
import com.awesome.yunpicturebackend.common.utils.ResultUtil;
import com.awesome.yunpicturebackend.exception.BusinessException;
import com.awesome.yunpicturebackend.model.dto.space.SpaceAddRequest;
import com.awesome.yunpicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.awesome.yunpicturebackend.model.entity.SpaceUser;
import com.awesome.yunpicturebackend.service.SpaceUserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/space_user")
public class SpaceUserController {

    @Resource
    private SpaceUserService spaceUserService;

    @PostMapping("/add")
    public BaseResponse<Boolean> addSpaceUser(@RequestBody SpaceUserAddRequest spaceUserAddRequest) {
        if (spaceUserAddRequest == null) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        return ResultUtil.success(spaceUserService.addSpaceUser(spaceUserAddRequest));
    }

    @DeleteMapping("/team_user")
    public BaseResponse<Boolean> deleteSpaceUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        // 1.校验参数
        if (deleteRequest == null) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        // 2.鉴权
        // 当前用户必须为空间管理员才能删除用户
        // 3.判断当前用户是否在团队空间内
        // 4.删除
        return ResultUtil.success(true);
    }

}
