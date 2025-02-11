package com.awesome.yunpicturebackend.controller;

import com.awesome.yunpicturebackend.annotation.ModulePermissionCheck;
import com.awesome.yunpicturebackend.common.BaseResponse;
import com.awesome.yunpicturebackend.common.DeleteRequest;
import com.awesome.yunpicturebackend.common.ResponseCode;
import com.awesome.yunpicturebackend.common.utils.ResultUtil;
import com.awesome.yunpicturebackend.exception.BusinessException;
import com.awesome.yunpicturebackend.model.dto.space.SpaceAddRequest;
import com.awesome.yunpicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.awesome.yunpicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.awesome.yunpicturebackend.model.entity.SpaceUser;
import com.awesome.yunpicturebackend.model.vo.spaceuser.SpaceUserInfo;
import com.awesome.yunpicturebackend.model.vo.spaceuser.SpaceUserVO;
import com.awesome.yunpicturebackend.service.SpaceUserService;
import com.awesome.yunpicturebackend.util.ValidateUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

    @ModulePermissionCheck
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpaceUser(@RequestBody DeleteRequest deleteRequest) {
        // 1.校验参数
        if (deleteRequest == null) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        if (ValidateUtil.isNullOrNotPositive(deleteRequest.getId()) && ValidateUtil.isNullOrEmpty(deleteRequest.getIds())) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        // 2.删除
        return ResultUtil.success(spaceUserService.removeById(deleteRequest.getId()));
    }

    @ModulePermissionCheck
    @PostMapping("/delete/batch")
    public BaseResponse<Boolean> deleteSpaceUsers(@RequestBody DeleteRequest deleteRequest) {
        // 1.校验参数
        if (deleteRequest == null) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        if (ValidateUtil.isNullOrEmpty(deleteRequest.getIds())) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        // 2.删除
        return ResultUtil.success(spaceUserService.removeBatchByIds(deleteRequest.getIds()));
    }

    @ModulePermissionCheck
    @PostMapping("/read/list/info")
    public BaseResponse<List<SpaceUserInfo>> listSpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest) {
        if (spaceUserQueryRequest == null) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR);
        }
        return ResultUtil.success(spaceUserService.listSpaceUserInfo(spaceUserQueryRequest));
    }

}
