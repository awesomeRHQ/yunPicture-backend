package com.awesome.yunpicturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import com.awesome.yunpicturebackend.annotation.AuthCheck;
import com.awesome.yunpicturebackend.common.BaseResponse;
import com.awesome.yunpicturebackend.common.ResponseCode;
import com.awesome.yunpicturebackend.common.utils.ResultUtil;
import com.awesome.yunpicturebackend.exception.ThrowUtil;
import com.awesome.yunpicturebackend.model.dto.user.*;
import com.awesome.yunpicturebackend.model.entity.User;
import com.awesome.yunpicturebackend.model.vo.user.LoginUserVO;
import com.awesome.yunpicturebackend.model.vo.user.UserVO;
import com.awesome.yunpicturebackend.service.UserService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        ThrowUtil.throwIf(userRegisterRequest == null, ResponseCode.PARAMS_ERROR);
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        return ResultUtil.success(userService.userRegister(userAccount,userPassword,checkPassword));
    }

    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        ThrowUtil.throwIf(userLoginRequest == null, ResponseCode.PARAMS_ERROR);
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        return ResultUtil.success(userService.userLogin(userAccount,userPassword,request));
    }

    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request){
        ThrowUtil.throwIf(request == null, ResponseCode.PARAMS_ERROR);
        return ResultUtil.success(userService.getLoginUserVO(userService.getLoginUser(request)));
    }

    @GetMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request){
        ThrowUtil.throwIf(request == null, ResponseCode.PARAMS_ERROR);
        return ResultUtil.success(userService.userLogout(request));
    }

    @AuthCheck(mustRole = "admin")
    @PostMapping("/add")
    public BaseResponse<Boolean> addUser(@RequestBody UserAddRequest userAddRequest){
        ThrowUtil.throwIf(userAddRequest == null, ResponseCode.PARAMS_ERROR);
        String userPassword = userAddRequest.getUserPassword();
        userAddRequest.setUserPassword(userService.getEncryptedPassword(userPassword));
        User newUser = new User();
        BeanUtil.copyProperties(userAddRequest,newUser);
        boolean result = userService.save(newUser);
        ThrowUtil.throwIf(!result, ResponseCode.SYSTEM_ERROR,"新增用户错误");
        return ResultUtil.success(result);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest){
        ThrowUtil.throwIf(userUpdateRequest == null, ResponseCode.PARAMS_ERROR);
        User user = new User();
        BeanUtil.copyProperties(userUpdateRequest,user);
        boolean result = userService.updateById(user);
        ThrowUtil.throwIf(!result, ResponseCode.SYSTEM_ERROR,"更新用户错误");
        return ResultUtil.success(result);
    }

    @AuthCheck(mustRole = "admin")
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest){
        // 1.校验参数
        ThrowUtil.throwIf(userQueryRequest == null, ResponseCode.PARAMS_ERROR);
        // 2.设置分页
        int current = userQueryRequest.getCurrent();
        int pageSize = userQueryRequest.getPageSize();
        IPage<User> userIPage = new Page<>(current, pageSize);
        // 3.拼接查询
        IPage<User> page = userService.page(userIPage, userService.getQueryWrapper(userQueryRequest));
        List<User> recordUserList = page.getRecords();
        // 4.处理结果
        // 将UserList转换为UserVOList
        List<UserVO> userVOList = userService.getUserVOList(recordUserList);
        // 再将UserVOList转换为UserVOPage
        Page<UserVO> userPage = new Page<>(current, pageSize, recordUserList.size());
        userPage.setRecords(userVOList);
        // 5.返回
        return ResultUtil.success(userPage);
    }
}
