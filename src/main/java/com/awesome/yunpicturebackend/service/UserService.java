package com.awesome.yunpicturebackend.service;

import com.awesome.yunpicturebackend.model.dto.user.UserQueryRequest;
import com.awesome.yunpicturebackend.model.entity.User;
import com.awesome.yunpicturebackend.model.vo.user.LoginUserVO;
import com.awesome.yunpicturebackend.model.vo.user.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author awesome
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2024-12-11 19:06:03
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return 用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏用户
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     * @return 当前登录用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取当前登录用户，不存在直接抛出异常，用于request校验
     * @return 当前登录用户
     */
    User getLoginUserIfExist(HttpServletRequest request);

    /**
     * 用户注销
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 登录后获取脱敏用户
     * @param user 当前用户
     * @return 脱敏后用户
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏用户
     * @param user 当前用户
     * @return 脱敏后用户
     */
    UserVO getUserVO(User user);

    /**
     * 通过用户id获取脱敏用户
     * @param id 用户id
     * @return
     */
    UserVO getUserVO(Long id);

    /**
     * 获取脱敏用户列表
     * @param userList 用户列表
     * @return 脱敏后用户列表
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 拼接查询条件
     * @param userQueryRequest 查询请求类
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 判断当前用户是否为管理员
     * @param user 当前用户
     * @return
     */
    boolean isAdmin(User user);

    /**
     * 密码加密
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    String getEncryptedPassword(String userPassword);

}
