package com.awesome.yunpicturebackend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.awesome.yunpicturebackend.common.ResponseCode;
import com.awesome.yunpicturebackend.exception.BusinessException;
import com.awesome.yunpicturebackend.model.enums.UserRoleEnum;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.awesome.yunpicturebackend.model.entity.User;
import com.awesome.yunpicturebackend.service.UserService;
import com.awesome.yunpicturebackend.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
* @author awesome
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2024-12-11 19:06:03
*/
@Service
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
        newUser.setUserRole(UserRoleEnum.USER.getValue());
        try {
            boolean result = this.save(newUser);
            if (!result){
                throw new BusinessException(ResponseCode.SYSTEM_ERROR,"注册，数据库插入错误");
            }
        }catch (RuntimeException e){
            throw new BusinessException(ResponseCode.SYSTEM_ERROR,"注册，数据库插入错误");
        }

        return newUser.getId();
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




