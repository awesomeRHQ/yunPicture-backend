package com.awesome.yunpicturebackend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.awesome.yunpicturebackend.common.ResponseCode;
import com.awesome.yunpicturebackend.constants.UserConstant;
import com.awesome.yunpicturebackend.exception.BusinessException;
import com.awesome.yunpicturebackend.exception.ThrowUtil;
import com.awesome.yunpicturebackend.model.dto.user.UserQueryRequest;
import com.awesome.yunpicturebackend.model.enums.SortOrderEnum;
import com.awesome.yunpicturebackend.model.enums.UserRoleEnum;
import com.awesome.yunpicturebackend.model.vo.user.LoginUserVO;
import com.awesome.yunpicturebackend.model.vo.user.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.awesome.yunpicturebackend.model.entity.User;
import com.awesome.yunpicturebackend.service.UserService;
import com.awesome.yunpicturebackend.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author awesome
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2024-12-11 19:06:03
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    /**
     * 用户注册
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 校验密码
     * @return 用户Id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1.校验参数
        if(StrUtil.hasBlank(userAccount,userPassword,checkPassword)){
            throw new BusinessException(ResponseCode.PARAMS_ERROR,"参数不能为空");
        }
        if(userAccount.length() < 4){
            throw new BusinessException(ResponseCode.PARAMS_ERROR,"账号长度不能小于4");
        }
        if(userPassword.length() < 6){
            throw new BusinessException(ResponseCode.PARAMS_ERROR,"密码长度不能小于6");
        }
        if (!userPassword.equals(checkPassword)){
            throw new BusinessException(ResponseCode.PARAMS_ERROR,"密码和校验密码不一致");
        }
        // 2.检查账号是否存在
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount",userAccount);
        User user = this.getOne(userQueryWrapper);
        if (user != null){
            throw new BusinessException(ResponseCode.PARAMS_ERROR,"用户已存在");
        }
        // 3.密码加密
        String encryptedPassword = getEncryptedPassword(userPassword);
        // 4.插入数据
        User newUser = new User();
        newUser.setUserAccount(userAccount);
        newUser.setUserPassword(encryptedPassword);
        newUser.setUserName("用户"+userAccount);
        newUser.setUserAvatar(UserConstant.USER_DEFAULT_AVATAR);
        newUser.setUserRole(UserRoleEnum.USER.getValue());
        try {
            boolean result = this.save(newUser);
            if (!result){
                throw new BusinessException(ResponseCode.SYSTEM_ERROR,"注册，数据库插入错误");
            }
        }catch (RuntimeException e){
            throw new BusinessException(ResponseCode.SYSTEM_ERROR,"注册，数据库插入错误");
        }
        // 5.返回
        return newUser.getId();
    }

    /**
     * 用户登录
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @return 脱敏用户
     */
    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1.参数校验
        if(StrUtil.hasBlank(userAccount,userPassword)){
            throw new BusinessException(ResponseCode.PARAMS_ERROR, "账号或密码为空");
        }
        // 2.查询用户是否存在
        // 加密密码
        String encryptedPassword = getEncryptedPassword(userPassword);
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("userAccount",userAccount).eq("userPassword",encryptedPassword);
        User user = this.getOne(userQueryWrapper);
        if (user == null){
            log.info("user login fail: course by user not found");
            throw new BusinessException(ResponseCode.NOT_FOUND_ERROR,"账号或密码错误");
        }
        // 3.存在则保存用户登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE,user);
        return getLoginUserVO(user);
    }

    /**
     * 获取当前登录用户
     * @param request
     * @return 当前登录用户
     */
    @Override
    public User getLoginUser(HttpServletRequest request){
        // 从缓存中获取当前用户
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);;
        if (currentUser == null){
            return null;
        }
        // todo 追求性能可以注释
        currentUser = this.getById(currentUser.getId());
        ThrowUtil.throwIf(currentUser == null ,ResponseCode.NOT_FOUND_ERROR, "当前用户在数据库中不存在");
        return currentUser;
    }

    /**
     * 用户注销
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        User currentUser = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        ThrowUtil.throwIf(currentUser == null,ResponseCode.NOT_FOUND_ERROR, "未登录");
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    /**
     * 登录后获取脱敏用户
     * @param user 当前用户
     * @return 脱敏后用户
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user,loginUserVO);
        return loginUserVO;
    }

    /**
     * 获取脱敏用户
     * @param user 当前用户
     * @return 脱敏后用户
     */
    @Override
    public UserVO getUserVO(User user) {
        return BeanUtil.copyProperties(user,UserVO.class);
    }

    /**
     * 通过用户id获取脱敏用户
     * @param id 用户id
     * @return
     */
    @Override
    public UserVO getUserVO(Long id) {
        if(id == null || id == 0){
            return null;
        }
        User user = this.getById(id);
        return getUserVO(user);
    }

    /**
     * 获取脱敏用户列表
     * @param userList 用户列表
     * @return 脱敏后用户列表
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (userList.isEmpty()){
            return null;
        }
        List<UserVO> userVOList = userList.stream().map(this::getUserVO).collect(Collectors.toList());
        return userVOList;
    }

    /**
     * 拼接查询条件
     * @param userQueryRequest 查询请求类
     * @return
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        // 1.校验参数
        ThrowUtil.throwIf(userQueryRequest == null, ResponseCode.PARAMS_ERROR, "查询参数为空");
        // 2.获取字段
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String userName = userQueryRequest.getUserName();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        // 3.拼接查询条件
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.like(id != null && id > 0 ,"id", id);
        queryWrapper.like(StrUtil.isNotBlank(userAccount),"userAccount",userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName),"userName",userName);
        queryWrapper.eq(StrUtil.isNotBlank(userRole),"userRole",userRole);
        queryWrapper.orderBy(StrUtil.isNotBlank(sortField),sortOrder.equals(SortOrderEnum.ASC.getValue()),sortField);
        // 4.返回
        return queryWrapper;
    }

    /**
     * 判断当前用户是否为管理员
     * @param user 当前用户
     * @return
     */
    @Override
    public boolean isAdmin(User user) {
        if (user == null){
            return false;
        } else {
            return UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
        }
    }

    /**
     * 密码加密
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    @Override
    public String getEncryptedPassword(String userPassword) {
        if (StrUtil.hasBlank(userPassword)){
            throw new BusinessException(ResponseCode.PARAMS_ERROR,"密码为空");
        }
        final String SALT = "awesome";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }
}




