package com.awesome.yunpicturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.awesome.yunpicturebackend.annotation.AuthCheck;
import com.awesome.yunpicturebackend.common.BaseResponse;
import com.awesome.yunpicturebackend.common.DeleteRequest;
import com.awesome.yunpicturebackend.common.ResponseCode;
import com.awesome.yunpicturebackend.common.utils.ResultUtil;
import com.awesome.yunpicturebackend.exception.BusinessException;
import com.awesome.yunpicturebackend.exception.ThrowUtil;
import com.awesome.yunpicturebackend.manager.CosManager;
import com.awesome.yunpicturebackend.model.dto.picture.PictureQueryRequest;
import com.awesome.yunpicturebackend.model.dto.picture.PictureUpdateRequest;
import com.awesome.yunpicturebackend.model.dto.picture.PictureUploadRequest;
import com.awesome.yunpicturebackend.model.entity.Picture;
import com.awesome.yunpicturebackend.model.entity.User;
import com.awesome.yunpicturebackend.model.enums.UserRoleEnum;
import com.awesome.yunpicturebackend.model.vo.picture.PictureTagCategory;
import com.awesome.yunpicturebackend.model.vo.picture.PictureVO;
import com.awesome.yunpicturebackend.service.PictureService;
import com.awesome.yunpicturebackend.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/picture")
@Slf4j
public class PictureController {

    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    @AuthCheck(mustRole = "admin")
    @PostMapping("/upload")
    public BaseResponse<PictureVO> uploadPicture(
            @RequestPart MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, pictureUploadRequest, loginUser);
        return ResultUtil.success(pictureVO);
    }

    @AuthCheck(mustRole = "admin")
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtil.throwIf(loginUser == null, ResponseCode.NOT_LOGIN_ERROR);
        ThrowUtil.throwIf(deleteRequest == null , ResponseCode.PARAMS_ERROR);
        Picture existPicture = pictureService.getById(deleteRequest.getId());
        ThrowUtil.throwIf(existPicture == null, ResponseCode.PARAMS_ERROR, "图片不存在");
        // 仅管理员和图片创建人可删除
        ThrowUtil.throwIf(!UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole()) || !Objects.equals(loginUser.getId(), existPicture.getUserId()),
                ResponseCode.NO_AUTH_ERROR,"当前用户无权限删除该图片");
        return ResultUtil.success(pictureService.removeById(deleteRequest.getId()));
    }

    @AuthCheck(mustRole = "admin")
    @PostMapping("/update")
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtil.throwIf(loginUser == null, ResponseCode.NOT_LOGIN_ERROR);
        ThrowUtil.throwIf(pictureUpdateRequest == null , ResponseCode.PARAMS_ERROR);
        // 仅管理员和图片创建人可更新图片
        ThrowUtil.throwIf(!UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole()) || !Objects.equals(loginUser.getId(), pictureUpdateRequest.getUserId()),
                ResponseCode.NO_AUTH_ERROR,"当前用户无权限编辑该图片");
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureUpdateRequest, picture);
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        return ResultUtil.success(pictureService.updateById(picture));
    }

    @AuthCheck(mustRole = "admin")
    @PostMapping("/page")
    public BaseResponse<Page<Picture>> getPicturePage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        ThrowUtil.throwIf(pictureQueryRequest == null , ResponseCode.PARAMS_ERROR);
        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
        Page<Picture> page = new Page<>(current, pageSize);
        Page<Picture> picturePage = pictureService.page(page, pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtil.success(picturePage);
    }

    @PostMapping("/page/vo")
    public BaseResponse<Page<PictureVO>> getPictureVOPage(@RequestBody PictureQueryRequest pictureQueryRequest) {
        ThrowUtil.throwIf(pictureQueryRequest == null , ResponseCode.PARAMS_ERROR);
        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
        // 对普通用户限制一次请求获取条数
        ThrowUtil.throwIf(pageSize > 20 , ResponseCode.PARAMS_ERROR, "请求过多");
        Page<Picture> picturePage = pictureService.page(new Page<>(current, pageSize), pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtil.success(pictureService.getPictureVOPage(picturePage));
    }

    @PostMapping("/get/vo")
    public BaseResponse<PictureVO> getPictureVOById(@RequestParam Long id) {
        ThrowUtil.throwIf(id == null , ResponseCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(id);
        ThrowUtil.throwIf(picture == null, ResponseCode.NOT_FOUND_ERROR,"图片不存在");
        return ResultUtil.success(pictureService.getPictureVO(picture));
    }

    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> tagList = Arrays.asList("热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意");
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setTagList(tagList);
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtil.success(pictureTagCategory);
    }


}
