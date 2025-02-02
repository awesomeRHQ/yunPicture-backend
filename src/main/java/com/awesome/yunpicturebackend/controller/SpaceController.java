package com.awesome.yunpicturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import com.awesome.yunpicturebackend.annotation.AuthCheck;
import com.awesome.yunpicturebackend.common.BaseResponse;
import com.awesome.yunpicturebackend.common.DeleteRequest;
import com.awesome.yunpicturebackend.common.ResponseCode;
import com.awesome.yunpicturebackend.common.utils.ResultUtil;
import com.awesome.yunpicturebackend.exception.BusinessException;
import com.awesome.yunpicturebackend.exception.ThrowUtil;
import com.awesome.yunpicturebackend.model.dto.space.SpaceAddRequest;
import com.awesome.yunpicturebackend.model.dto.space.SpaceEditRequest;
import com.awesome.yunpicturebackend.model.dto.space.SpaceQueryRequest;
import com.awesome.yunpicturebackend.model.dto.space.SpaceUpdateRequest;
import com.awesome.yunpicturebackend.model.entity.Space;
import com.awesome.yunpicturebackend.model.entity.User;
import com.awesome.yunpicturebackend.model.enums.space.SpaceLevelEnum;
import com.awesome.yunpicturebackend.model.enums.UserRoleEnum;
import com.awesome.yunpicturebackend.model.vo.space.SpaceLevel;
import com.awesome.yunpicturebackend.service.SpaceService;
import com.awesome.yunpicturebackend.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/space")
public class SpaceController {

    @Resource
    private UserService userService;

    @Resource
    private SpaceService spaceService;

    @PostMapping("/save")
    public BaseResponse<Boolean> saveSpace(@RequestBody SpaceAddRequest spaceAddRequest) {
        // 1.校验请求参数
        ThrowUtil.throwIf(spaceAddRequest == null, ResponseCode.PARAMS_ERROR);
        Space space = BeanUtil.copyProperties(spaceAddRequest, Space.class);
        // 2.校验数据
        spaceService.validateSpace(space,true);
        // 3.赋值
        spaceService.fillSpaceBySpaceLevel(space);
        space.setSpaceType(spaceAddRequest.getSpaceType());
        space.setUserId(spaceAddRequest.getUserId());
        // 4.新增
        boolean result = spaceService.save(space);
        return ResultUtil.success(result);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteSpace(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        // 1.校验请求参数
        ThrowUtil.throwIf(deleteRequest == null, ResponseCode.PARAMS_ERROR);
        Space existSpace = spaceService.getById(deleteRequest.getId());
        ThrowUtil.throwIf(existSpace == null, ResponseCode.NOT_FOUND_ERROR,"当前空间不存在");
        User loginUser = userService.getLoginUserIfExist(request);
        // 2.校验权限
        // 用户为管理员，或者为空间创建人才允许删除
        if (!UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole()) && loginUser.getId() != existSpace.getUserId()) {
            throw new BusinessException(ResponseCode.NO_AUTH_ERROR,"当前用户无删除空间权限");
        }
        // 3.执行删除
        return ResultUtil.success(spaceService.deleteSpace(existSpace));
    }

    @AuthCheck(mustRole = "admin")
    @PostMapping("/update")
    public BaseResponse<Boolean> updateSpace(@RequestBody SpaceUpdateRequest spaceUpdateRequest, HttpServletRequest request) {
        // 1.参数校验
        ThrowUtil.throwIf(spaceUpdateRequest == null, ResponseCode.PARAMS_ERROR);
        ThrowUtil.throwIf(spaceUpdateRequest.getId() == null || spaceUpdateRequest.getId() <= 0, ResponseCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUserIfExist(request);
        // 2.赋值
        Space space = new Space();
        BeanUtils.copyProperties(spaceUpdateRequest, space);
        // 3.校验数据
        spaceService.validateSpace(space,false);
        // 若存在空间等级且空间最大容量和最大数量都为0时，默认大小赋值
        if (space.getSpaceLevel() >= 0){
            if (space.getMaxSize() == null || space.getMaxCount() == null){
                spaceService.fillSpaceBySpaceLevel(space);
            }
        }
        // 4.更新
        return ResultUtil.success(spaceService.updateById(space));
    }

    @PostMapping("/edit")
    public BaseResponse<Boolean> editSpace(@RequestBody SpaceEditRequest spaceEditRequest, HttpServletRequest request) {
        // 1.参数校验
        ThrowUtil.throwIf(spaceEditRequest == null, ResponseCode.PARAMS_ERROR);
        ThrowUtil.throwIf(spaceEditRequest.getId() == null || spaceEditRequest.getId() <= 0, ResponseCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUserIfExist(request);
        // 2.赋值
        Space space = new Space();
        BeanUtils.copyProperties(spaceEditRequest, space);
        // 3.校验数据
        spaceService.validateSpace(space,false);
        // 4.更新
        return ResultUtil.success(spaceService.updateById(space));
    }

    @GetMapping("/get")
    public BaseResponse<Space> getSpace(@RequestParam Long spaceId) {
        ThrowUtil.throwIf(spaceId == null, ResponseCode.PARAMS_ERROR);
        return ResultUtil.success(spaceService.getById(spaceId));
    }

    @GetMapping("/private_id")
    public BaseResponse<Long> getLoginUserPrivateSpaceId(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtil.throwIf(loginUser == null, ResponseCode.NOT_LOGIN_ERROR);
        Space privateSpace = spaceService.lambdaQuery().eq(Space::getUserId, loginUser.getId()).eq(Space::getSpaceType, 0).one();
        if (privateSpace == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND_ERROR);
        }
        return ResultUtil.success(privateSpace.getId());
    }

    @GetMapping("/list/level")
    public BaseResponse<List<SpaceLevel>> listSpaceLevel() {
        List<SpaceLevel> spaceLevelList = Arrays.stream(SpaceLevelEnum.values()) // 获取所有枚举
                .map(spaceLevelEnum -> new SpaceLevel(
                        spaceLevelEnum.getValue(),
                        spaceLevelEnum.getText(),
                        spaceLevelEnum.getMaxCount(),
                        spaceLevelEnum.getMaxSize()))
                .collect(Collectors.toList());
        return ResultUtil.success(spaceLevelList);
    }

    @PostMapping("/list/page")
    public BaseResponse<Page<Space>> pageSpace(@RequestBody SpaceQueryRequest spaceQueryRequest,
                                                 HttpServletRequest request) {
        // 1.校验参数
        ThrowUtil.throwIf(spaceQueryRequest == null, ResponseCode.PARAMS_ERROR);
        // 2.page查询
        int current = spaceQueryRequest.getCurrent();
        int pageSize = spaceQueryRequest.getPageSize();
        QueryWrapper spaceQueryWrapper = spaceService.getQueryWrapper(spaceQueryRequest);
        Page spacePage = spaceService.page(new Page<>(current, pageSize), spaceQueryWrapper);
        // 3.Space -> SpaceVO
//        List<Space> spaceRecords = spacePage.getRecords();
//        List<SpaceVO> spaceVOList = new ArrayList<>();
//        spaceRecords.forEach(space -> {
//            SpaceVO spaceVO = SpaceVO.objToVo(space);
//            spaceVO.setUser(userService.getUserVO(space.getUserId()));
//            spaceVOList.add(spaceVO);
//        });
//        // 4.组合SpaceVOPage返回
//        Page<SpaceVO> spaceVOPage = new Page<>();
//        spaceVOPage.setCurrent(spacePage.getCurrent());
//        spaceVOPage.setSize(spacePage.getSize());
//        spaceVOPage.setTotal(spacePage.getTotal());
//        spaceVOPage.setRecords(spaceVOList);
        return ResultUtil.success(spacePage);
    }


}
