package com.awesome.yunpicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.awesome.yunpicturebackend.common.ResponseCode;
import com.awesome.yunpicturebackend.exception.BusinessException;
import com.awesome.yunpicturebackend.exception.ThrowUtil;
import com.awesome.yunpicturebackend.mapper.SpaceMapper;
import com.awesome.yunpicturebackend.model.dto.space.SpaceAddRequest;
import com.awesome.yunpicturebackend.model.dto.space.SpaceQueryRequest;
import com.awesome.yunpicturebackend.model.entity.Picture;
import com.awesome.yunpicturebackend.model.entity.Space;
import com.awesome.yunpicturebackend.model.entity.User;
import com.awesome.yunpicturebackend.model.enums.SortOrderEnum;
import com.awesome.yunpicturebackend.model.enums.SpaceLevelEnum;
import com.awesome.yunpicturebackend.model.enums.UserRoleEnum;
import com.awesome.yunpicturebackend.model.vo.space.SpaceVO;
import com.awesome.yunpicturebackend.service.PictureService;
import com.awesome.yunpicturebackend.service.SpaceService;
import com.awesome.yunpicturebackend.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 针对表【space(空间)】的服务Service实现类
 *
 * @author awesomeRHQ
 * @since 2025-01-15 14:21:37
 */
@Service("SpaceService")
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceService {

    @Resource
    private UserService userService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    @Lazy
    private PictureService pictureService;

    /**
     * 验证空间信息
     * @param space 空间
     * @param add 判断新增还是更新
     */
    @Override
    public void validateSpace(Space space, boolean add) {
        ThrowUtil.throwIf(space == null, ResponseCode.PARAMS_ERROR);
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        // 创建时，空间名称和级别不能为空
        if (add) {
            ThrowUtil.throwIf(StrUtil.isBlank(spaceName), ResponseCode.PARAMS_ERROR,"空间名称不能为空");
            ThrowUtil.throwIf(spaceLevel == null, ResponseCode.PARAMS_ERROR,"空间级别不能为空");
        }
        if (StrUtil.isNotBlank(spaceName) && spaceName.length() > 20) {
            throw  new BusinessException(ResponseCode.PARAMS_ERROR, "空间名称过长");
        }
        if (spaceLevel != null && spaceLevelEnum == null) {
            throw new BusinessException(ResponseCode.PARAMS_ERROR,"空间级别不存在");
        }
    }

    /**
     * 根据空间级别自动填充空间限额数据
     * @param space 空间
     */
    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        ThrowUtil.throwIf(space == null, ResponseCode.PARAMS_ERROR);
        Integer spaceLevel = space.getSpaceLevel();
        ThrowUtil.throwIf(spaceLevel == null, ResponseCode.PARAMS_ERROR);
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);
        ThrowUtil.throwIf(spaceLevelEnum == null, ResponseCode.PARAMS_ERROR, "空间级别不存在");
        space.setMaxSize(spaceLevelEnum.getMaxSize());
        space.setMaxCount(spaceLevelEnum.getMaxCount());
    }

    /**
     * 删除空间
     * @param space 空间
     * @return 删除结果
     */
    @Override
    @Transactional
    public boolean deleteSpace(Space space) {
        if (space == null) {
            log.error("删除的空间不存在");
            return false;
        }
        // todo 若空间内含有图片，则先删除空间内的所有图片
        if (space.getTotalCount() > 0){
            QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
            pictureQueryWrapper.eq("spaceId", space.getId());
            List<Picture> pictureList = pictureService.list(pictureQueryWrapper);
            pictureService.deletePictureByPictureList(pictureList,true);
        }
        // 删除空间
        return this.removeById(space.getId());
    }

    /**
     * 初始化私密空间
     * @param loginUser 登录用户
     * @return 空间对象
     */
    @Override
    public SpaceVO initPrivateSpace(User loginUser) {
        // 1.参数校验
        ThrowUtil.throwIf(loginUser == null, ResponseCode.NOT_LOGIN_ERROR);
        // 2.填充参数默认值
        Space privateSpace = new Space();
        privateSpace.setSpaceName("我的私人空间");
        privateSpace.setSpaceType(0);
        privateSpace.setSpaceLevel(0);
        privateSpace.setTotalSize(0L);
        privateSpace.setTotalCount(0L);
        privateSpace.setUserId(loginUser.getId());
        this.fillSpaceBySpaceLevel(privateSpace);
        // 3.创建
        this.save(privateSpace);
        // 4.类型转换
        SpaceVO privateSpaceVO = SpaceVO.objToVo(privateSpace);
        privateSpaceVO.setUser(userService.getUserVO(privateSpace.getUserId()));
        return privateSpaceVO;
    }

    /**
     * 创建空间
     * @param spaceAddRequest 创建空间请求对象
     * @param loginUser 登录用户
     * @return 空间对象
     */
    @Override
    public SpaceVO createSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        // 0.参数校验
        ThrowUtil.throwIf(spaceAddRequest == null, ResponseCode.PARAMS_ERROR);
        ThrowUtil.throwIf(loginUser == null, ResponseCode.NOT_LOGIN_ERROR);
        Space space = new Space();
        BeanUtils.copyProperties(spaceAddRequest, space);
        // 1.校验权限
        // 非管理员默认只能创建普通空间
        if (!UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole())){
            space.setSpaceLevel(0);
        }
        // 2.参数校验 填充参数默认值
        this.validateSpace(space,true);
        space.setSpaceType(1);
        space.setTotalSize(0L);
        space.setTotalCount(0L);
        space.setUserId(loginUser.getId());
        this.fillSpaceBySpaceLevel(space);
        // 3.创建
        // 针对用户进行加锁
        String lock = String.valueOf(loginUser.getId()).intern();
        synchronized (lock) {
            transactionTemplate.execute(status -> {
                // 确保一个用户只能创建一个私有空间
                boolean exists = this.lambdaQuery().eq(Space::getSpaceType, 0).eq(Space::getUserId, loginUser.getId()).exists();
                if (exists) {
                    throw new BusinessException(ResponseCode.OPERATION_ERROR,"每个用户只能创建一个私有空间");
                }
                boolean saveResult = this.save(space);
                ThrowUtil.throwIf(!saveResult, ResponseCode.OPERATION_ERROR);
                return space.getId();
            });
        }
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        spaceVO.setUser(userService.getUserVO(loginUser.getId()));
        return spaceVO;
    }

    /**
     * 检查空间限额
     * @param spaceId 空间id
     */
    @Override
    public void checkSpaceQuota(Long spaceId) {
        Space space = this.getById(spaceId);
        ThrowUtil.throwIf(space == null, ResponseCode.OPERATION_ERROR, "空间不存在");
        // 检查限额
        if (space.getMaxCount() == space.getTotalCount()){
            throw new BusinessException(ResponseCode.OPERATION_ERROR,"已达空间最大图片数量");
        }
        if (space.getMaxSize() == space.getTotalSize()){
            throw new BusinessException(ResponseCode.OPERATION_ERROR,"已达空间最大容量");
        }
    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        Integer spaceType = spaceQueryRequest.getSpaceType();
        String sortField = spaceQueryRequest.getSortField();
        String sortOrder = spaceQueryRequest.getSortOrder();

        QueryWrapper<Space> spaceQueryWrapper = new QueryWrapper<>();
        spaceQueryWrapper.like(id != null && id > 0, "id", id);
        spaceQueryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);
        spaceQueryWrapper.eq(spaceLevel != null && spaceLevel >=0, "spaceLevel", spaceLevel);
        spaceQueryWrapper.eq(spaceType != null && spaceType >=0, "spaceType", spaceType);
        spaceQueryWrapper.eq(userId != null && userId >0, "userId", userId);

        spaceQueryWrapper.orderBy(StrUtil.isNotBlank(sortField), SortOrderEnum.ASC.getValue().equals(sortOrder), sortField);
        return spaceQueryWrapper;
    }


}
