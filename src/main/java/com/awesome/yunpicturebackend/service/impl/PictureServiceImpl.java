package com.awesome.yunpicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.awesome.yunpicturebackend.annotation.AuthCheck;
import com.awesome.yunpicturebackend.common.ResponseCode;
import com.awesome.yunpicturebackend.exception.ThrowUtil;
import com.awesome.yunpicturebackend.manager.FileManager;
import com.awesome.yunpicturebackend.mapper.PictureMapper;
import com.awesome.yunpicturebackend.model.dto.file.UploadPictureResult;
import com.awesome.yunpicturebackend.model.dto.picture.PictureLoadMoreRequest;
import com.awesome.yunpicturebackend.model.dto.picture.PictureQueryRequest;
import com.awesome.yunpicturebackend.model.dto.picture.PictureUploadRequest;
import com.awesome.yunpicturebackend.model.entity.Picture;
import com.awesome.yunpicturebackend.model.entity.User;
import com.awesome.yunpicturebackend.model.enums.SortOrderEnum;
import com.awesome.yunpicturebackend.model.vo.picture.PictureVO;
import com.awesome.yunpicturebackend.service.PictureService;
import com.awesome.yunpicturebackend.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.classgraph.json.Id;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 针对表【picture(图片表)】的服务Service实现类
 *
 * @author awesomeRHQ
 * @since 2024-12-23 16:23:58
 */
@Service("PictureService")
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {

    @Resource
    private FileManager fileManager;

    @Resource
    private UserService userService;

    @Resource
    private PictureMapper pictureMapper;


    /**
     * 图片上传
     *
     * @param multipartFile        文件流
     * @param pictureUploadRequest 图片id（鉴别添加删除）
     * @param loginUser            登录用户
     * @return 脱敏图片信息
     */
    @Override
    public PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser) {
        // 1.数据校验
        ThrowUtil.throwIf(multipartFile == null, ResponseCode.NOT_LOGIN_ERROR, "文件不存在，图片上传错误");
        ThrowUtil.throwIf(loginUser == null, ResponseCode.NOT_LOGIN_ERROR, "用户未登录，图片上传错误");
        // 1.1判断是更新还是新增
        Long pictureId = null;
        if (pictureUploadRequest != null) {
            if (pictureUploadRequest.getId() != null && pictureUploadRequest.getId() > 0) {
                pictureId = pictureUploadRequest.getId();
            }
        }
        // 2.图片上传
        // 2.1指定用户上传文件
        String publicPathPrefix = "/public";
        String userUpdatePathPrefix = publicPathPrefix + '/' + loginUser.getId();
        UploadPictureResult uploadPictureResult = fileManager.uploadPictureObject(multipartFile, userUpdatePathPrefix);
        // 3.处理结果
        Picture picture = new Picture();
        if (pictureId != null) {
            // 若是更新
            picture = this.getById(pictureId);
            ThrowUtil.throwIf(picture == null, ResponseCode.NOT_FOUND_ERROR, "图片不存在，更新图片信息失败");
            picture.setEditTime(new Date());
        } else {
            // 若是新增
            picture.setUserId(loginUser.getId());
        }
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setName(uploadPictureResult.getName());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        boolean res = this.saveOrUpdate(picture);
        ThrowUtil.throwIf(!res, ResponseCode.SYSTEM_ERROR, "图片保存数据库错误");
        return this.getPictureVO(picture);
    }

    /**
     * 图片信息脱敏
     * @param picture 图片信息
     * @return 脱敏后的图片信息
     */
    @Override
    public PictureVO getPictureVO(Picture picture) {
        if (picture == null) {
            return null;
        }
        PictureVO pictureVO = new PictureVO();
        BeanUtils.copyProperties(picture, pictureVO);
        pictureVO.setTags(JSONUtil.toList(picture.getTags(),String.class));
        if (picture.getUserId() > 0){
            pictureVO.setUserVO(userService.getUserVO(picture.getUserId()));
        }
        return pictureVO;
    }

    /**
     * 获取图片列表
     * @param pictureQueryRequest 查询条件
     * @return 图片列表
     */
    @Override
    public List<Picture> getPictureList(PictureQueryRequest pictureQueryRequest) {
        List<Picture> pictureList = new ArrayList<>();
        if (pictureQueryRequest != null) {
            QueryWrapper<Picture> queryWrapper = getQueryWrapper(pictureQueryRequest);
            pictureList = this.list(queryWrapper);
        }
        return pictureList;
    }

    /**
     * 获取图片列表
     * @param pictureList 图片列表
     * @return 图片列表
     */
    @Override
    public List<PictureVO> getPictureVOList(List<Picture> pictureList) {
        List<PictureVO> pictureVOList = new ArrayList<>();
        if (!pictureList.isEmpty()) {
            // todo 当前获取每个图片的用户信息都进行一次查询 优化点：当用户重复时可以直接使用已有用户信息，即将用户信息单独查询出来，并按需获取
            pictureVOList = pictureList.stream().map(this::getPictureVO).collect(Collectors.toList());
        }
        return pictureVOList;
    }

    /**
     * 获取脱敏图片分页列表
     * @param picturePage 图片分页列表
     * @return 脱敏图片分页列表
     */
    @Override
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage) {
        if (picturePage == null) {
            return new Page<>();
        }
        // 1.获取基础数据
        List<Picture> pictureList = picturePage.getRecords();
        long current = picturePage.getCurrent();
        long size = picturePage.getSize();
        long total = picturePage.getTotal();
        // 2.将picture转化为pictureVO
        List<PictureVO> pictureVOList = getPictureVOList(pictureList);
        // 3.构造新page
        Page<PictureVO> pictureVOPage = new Page<>(current, size, total);
        pictureVOPage.setRecords(pictureVOList);
        // 4.返回
        return pictureVOPage;
    }

    /**
     * 分批获取脱敏图片列表（用于未登录用户）
     * @param pictureLoadMoreRequest 查询请求对象
     * @return 图片列表
     */
    @Override
    public List<PictureVO> listPictureVOBatch(PictureLoadMoreRequest pictureLoadMoreRequest) {
        int current = pictureLoadMoreRequest.getCurrent();
        int pageSize = pictureLoadMoreRequest.getPageSize();
        // 为了调用 getQueryWrapper 方法创建匹配的参数对象
        PictureQueryRequest pictureQueryRequest = new PictureQueryRequest();
        BeanUtils.copyProperties(pictureLoadMoreRequest, pictureQueryRequest);
        Page<Picture> picturePage = this.page(new Page<>(current, pageSize), getQueryWrapper(pictureQueryRequest));
        return this.getPictureVOList(picturePage.getRecords());
    }

    /**
     * 分批获取推荐脱敏图片列表（用于登录用户）
     * @param pictureLoadMoreRequest 查询请求对象
     * @return 图片列表
     */
    @AuthCheck(mustRole = "user")
    @Override
    public List<PictureVO> listRecommendPictureVOBatch(PictureLoadMoreRequest pictureLoadMoreRequest) {
        int current = pictureLoadMoreRequest.getCurrent();
        int pageSize = pictureLoadMoreRequest.getPageSize();
        // 为了调用 getQueryWrapper 方法创建匹配的参数对象
        PictureQueryRequest pictureQueryRequest = new PictureQueryRequest();
        BeanUtils.copyProperties(pictureLoadMoreRequest, pictureQueryRequest);
        // todo 结合用户的搜索偏好返回数据
        Page<Picture> picturePage = this.page(new Page<>(current, pageSize), getQueryWrapper(pictureQueryRequest));
        return this.getPictureVOList(picturePage.getRecords());
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        Long id = pictureQueryRequest.getId();
        String name = pictureQueryRequest.getName();
        String introduction = pictureQueryRequest.getIntroduction();
        String category = pictureQueryRequest.getCategory();
        List<String> tagList = pictureQueryRequest.getTags();
        String searchText = pictureQueryRequest.getSearchText();
        Long userId = pictureQueryRequest.getUserId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();

        QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
        pictureQueryWrapper.like(id != null  && id > 0, "id", id);
        pictureQueryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        pictureQueryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        pictureQueryWrapper.eq(StrUtil.isNotBlank(category), "category", category);
        pictureQueryWrapper.and(StrUtil.isNotBlank(searchText),
                q -> q.or(
                        i -> i.like("name", searchText).or().like("introduction", searchText).or().like("tags",searchText)
                )
        );
        // JSON数组查询
        if (CollUtil.isNotEmpty(tagList)) {
            tagList.forEach(tag -> {
                pictureQueryWrapper.like("tags", "\"" + tag + "\"");
            });
        }
        pictureQueryWrapper.eq(userId != null && userId > 0, "userId", userId);
        pictureQueryWrapper.orderBy(StrUtil.isNotBlank(sortField), SortOrderEnum.ASC.getValue().equals(sortOrder), sortField);
        return pictureQueryWrapper;
    }
}
