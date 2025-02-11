package com.awesome.yunpicturebackend.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.awesome.yunpicturebackend.annotation.AuthCheck;
import com.awesome.yunpicturebackend.annotation.ModulePermissionCheck;
import com.awesome.yunpicturebackend.common.BaseResponse;
import com.awesome.yunpicturebackend.common.DeleteRequest;
import com.awesome.yunpicturebackend.common.ResponseCode;
import com.awesome.yunpicturebackend.common.utils.ResultUtil;
import com.awesome.yunpicturebackend.constants.UserConstant;
import com.awesome.yunpicturebackend.exception.BusinessException;
import com.awesome.yunpicturebackend.exception.ThrowUtil;
import com.awesome.yunpicturebackend.model.dto.picture.*;
import com.awesome.yunpicturebackend.model.entity.Picture;
import com.awesome.yunpicturebackend.model.entity.User;
import com.awesome.yunpicturebackend.model.enums.UserRoleEnum;
import com.awesome.yunpicturebackend.model.vo.picture.PictureTagCategory;
import com.awesome.yunpicturebackend.model.vo.picture.PictureVO;
import com.awesome.yunpicturebackend.service.PictureService;
import com.awesome.yunpicturebackend.service.TagService;
import com.awesome.yunpicturebackend.service.UserService;
import com.awesome.yunpicturebackend.util.ColorSimilarityUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/picture")
@Slf4j
public class PictureController {

    @Resource
    private TagService tagService;

    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    /**
     * 上传图片
     */
    @ModulePermissionCheck
    @PostMapping("/add/upload")
    public BaseResponse<PictureVO> uploadPictureByFile(
            @RequestPart MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, loginUser,pictureUploadRequest);
        return ResultUtil.success(pictureVO);
    }

    /**
     * 上传图片
     */
    @ModulePermissionCheck
    @PostMapping("/add/upload/url")
    public BaseResponse<PictureVO> uploadPictureByUrl(
            @RequestParam String pictureUrl,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(pictureUrl, loginUser,pictureUploadRequest);
        return ResultUtil.success(pictureVO);
    }

    @ModulePermissionCheck
    @PostMapping("/update/upload")
    public BaseResponse<PictureVO> updatePictureByFile(
            @RequestPart MultipartFile multipartFile,
            PictureUploadRequest pictureUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        PictureVO pictureVO = pictureService.uploadPicture(multipartFile, loginUser,pictureUploadRequest);
        return ResultUtil.success(pictureVO);
    }

    /**
     * 管理员批量爬取导入图片
     */
    @ModulePermissionCheck
    @PostMapping("/manage/upload/batch")
    public BaseResponse<Integer> uploadPictureByBatch(
            @RequestBody PictureUploadByBatchRequest pictureUploadByBatchRequest,
            HttpServletRequest request
    ) {
        ThrowUtil.throwIf(pictureUploadByBatchRequest == null, ResponseCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        int uploadCount = pictureService.uploadPictureByBatch(pictureUploadByBatchRequest, loginUser);
        if (uploadCount > 0 && CollUtil.isNotEmpty(pictureUploadByBatchRequest.getTagList())) {
            // 保存图片标签
            tagService.saveTags(pictureUploadByBatchRequest.getTagList(),uploadCount);
        }
        return ResultUtil.success(uploadCount);
    }

    /**
     * 删除单张图片
     */
    @ModulePermissionCheck
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePicture(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtil.throwIf(loginUser == null, ResponseCode.NOT_LOGIN_ERROR);
        ThrowUtil.throwIf(deleteRequest == null , ResponseCode.PARAMS_ERROR);
        Picture existPicture = pictureService.getById(deleteRequest.getId());
        ThrowUtil.throwIf(existPicture == null, ResponseCode.PARAMS_ERROR, "图片不存在");
        // 仅管理员和图片创建人可删除
        if (!UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole()) && !loginUser.getId().equals(existPicture.getUserId())){
            throw new BusinessException(ResponseCode.NO_AUTH_ERROR,"当前用户无权限删除该图片");
        }
        boolean res = pictureService.deletePicture(existPicture, true);
        return ResultUtil.success(res);
    }

    /**
     * 批量删除图片
     */
    @ModulePermissionCheck
    @PostMapping("/delete/pictures")
    public BaseResponse<Boolean> deletePictures(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtil.throwIf(loginUser == null, ResponseCode.NOT_LOGIN_ERROR);
        ThrowUtil.throwIf(deleteRequest == null , ResponseCode.PARAMS_ERROR);
        // todo 批量图片鉴别创建人
        // 仅管理员和图片创建人可删除
//        if (!UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole()) || !loginUser.getId().equals(existPicture.getUserId())){
//            throw new BusinessException(ResponseCode.NO_AUTH_ERROR,"当前用户无权限删除该图片");
//        }
        List<Long> ids = deleteRequest.getIds();
        if (ids.isEmpty()){
            return ResultUtil.success(true);
        }
        boolean res = pictureService.deletePictureByIds(ids, true);
        return ResultUtil.success(res);
    }

    /**
     * 更新图片（管理员）
     */
    @ModulePermissionCheck
    @PostMapping("/update")
    public BaseResponse<Boolean> updatePicture(@RequestBody PictureUpdateRequest pictureUpdateRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtil.throwIf(loginUser == null, ResponseCode.NOT_LOGIN_ERROR);
        ThrowUtil.throwIf(pictureUpdateRequest == null , ResponseCode.PARAMS_ERROR);
        // 仅管理员和图片创建人可更新图片
        ThrowUtil.throwIf(!UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole()) && !loginUser.getId().equals(pictureUpdateRequest.getUserId()),
                ResponseCode.NO_AUTH_ERROR,"当前用户无权限编辑该图片");
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureUpdateRequest, picture);
        // 管理员或审核员更新默认通过
        pictureService.setReviewStatue(picture,1,"管理员更新默认通过", loginUser.getId());
        picture.setTags(JSONUtil.toJsonStr(pictureUpdateRequest.getTags()));
        return ResultUtil.success(pictureService.updateById(picture));
    }

    /**
     * 更新图片（普通用户）
     */
    @ModulePermissionCheck
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPicture(@RequestBody PictureEditRequest pictureEditRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtil.throwIf(loginUser == null, ResponseCode.NOT_LOGIN_ERROR);
        ThrowUtil.throwIf(pictureEditRequest == null , ResponseCode.PARAMS_ERROR);
        // 仅创建人可更新图片
        ThrowUtil.throwIf(!pictureEditRequest.getUserId().equals(loginUser.getId()),
                ResponseCode.NO_AUTH_ERROR,"当前用户无权限编辑该图片");
        Picture picture = new Picture();
        BeanUtil.copyProperties(pictureEditRequest, picture);
        // 修改后重新审批
        pictureService.setReviewStatue(picture,0,"", 0L);
        picture.setTags(JSONUtil.toJsonStr(pictureEditRequest.getTags()));
        return ResultUtil.success(pictureService.updateById(picture));
    }

    /**
     *
     */
    @ModulePermissionCheck
    @PostMapping("/manage/page")
    public BaseResponse<Page<Picture>> pagePicture(@RequestBody PictureAdminQueryRequest pictureAdminQueryRequest) {
        ThrowUtil.throwIf(pictureAdminQueryRequest == null , ResponseCode.PARAMS_ERROR);
        int current = pictureAdminQueryRequest.getCurrent();
        int pageSize = pictureAdminQueryRequest.getPageSize();
        Page<Picture> page = new Page<>(current, pageSize);
        Page<Picture> picturePage = pictureService.page(page, pictureService.getQueryWrapper(pictureAdminQueryRequest));
        return ResultUtil.success(picturePage);
    }

    /**
     * 获取图片VO分页数据
     */
    @PostMapping("/read/page/vo")
    public BaseResponse<Page<PictureVO>> pagePictureVO(@RequestBody PictureQueryRequest pictureQueryRequest) {
        ThrowUtil.throwIf(pictureQueryRequest == null , ResponseCode.PARAMS_ERROR);
        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
        // 对普通用户限制一次请求获取条数
        ThrowUtil.throwIf(pageSize > 50 , ResponseCode.PARAMS_ERROR, "请求过多");
        // 限定普通用户只能查看审核通过的图片
        Page<Picture> picturePage = pictureService.page(new Page<>(current, pageSize), pictureService.getQueryWrapper(pictureQueryRequest));
        return ResultUtil.success(pictureService.getPictureVOPage(picturePage));
    }

    /**
     * 获取用户个人空间图片分页数据
     */
    @PostMapping("/read/page/personal/vo")
    public BaseResponse<Page<PictureVO>> pagePersonalPictureVO(@RequestBody PicturePersonalQueryRequest picturePersonalQueryRequest) {
        ThrowUtil.throwIf(picturePersonalQueryRequest == null , ResponseCode.PARAMS_ERROR);
        int current = picturePersonalQueryRequest.getCurrent();
        int pageSize = picturePersonalQueryRequest.getPageSize();
        // 获取查询的图片列表
        List<PictureVO> pictureVOList = pictureService.listPersonalPictureVO(picturePersonalQueryRequest, StrUtil.isNotBlank(picturePersonalQueryRequest.getPicColor()));
        // 组装page
        Page<PictureVO> pictureVOPage = new Page<>(current, pageSize, pictureVOList.size());
        pictureVOPage.setRecords(pictureVOList);
        return ResultUtil.success(pictureVOPage);
    }

    @PostMapping("/read/batch/vo")
    public BaseResponse<List<PictureVO>> listPictureVOBatch(@RequestBody PictureQueryRequest pictureQueryRequest ,HttpServletRequest request) {
        ThrowUtil.throwIf(pictureQueryRequest == null , ResponseCode.PARAMS_ERROR);
        int pageSize = pictureQueryRequest.getPageSize();
        // 对普通用户限制一次请求获取条数
        ThrowUtil.throwIf(pageSize > 60 , ResponseCode.PARAMS_ERROR, "请求过多");
        User loginUser = userService.getLoginUser(request);
        // 区分是否为已登录用户查询
        if (loginUser == null) {
            List<PictureVO> pictureVOS = pictureService.listPictureVOBatch(pictureQueryRequest);
            return ResultUtil.success(pictureVOS);
        } else {
            // 未登录用户返回热门标签图片
            return ResultUtil.success(pictureService.listRecommendPictureVOBatch(pictureQueryRequest));
        }
    }

    @GetMapping("/read/vo")
    public BaseResponse<PictureVO> getPictureVOById(@RequestParam Long id) {
        ThrowUtil.throwIf(id == null , ResponseCode.PARAMS_ERROR);
        Picture picture = pictureService.getById(id);
        ThrowUtil.throwIf(picture == null, ResponseCode.NOT_FOUND_ERROR,"图片不存在");
        return ResultUtil.success(pictureService.getPictureVO(picture));
    }

    @GetMapping("/tag_category")
    public BaseResponse<PictureTagCategory> listPictureTagCategory() {
        PictureTagCategory pictureTagCategory = new PictureTagCategory();
        List<String> categoryList = Arrays.asList("模板", "电商", "表情包", "素材", "海报");
        pictureTagCategory.setCategoryList(categoryList);
        return ResultUtil.success(pictureTagCategory);
    }

    @ModulePermissionCheck
    @PostMapping("/manage/review")
    public BaseResponse<Boolean> doReview(@RequestBody PictureReviewRequest pictureReviewRequest, HttpServletRequest request) {
        // 获取校验信息
        ThrowUtil.throwIf(pictureReviewRequest == null , ResponseCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        ThrowUtil.throwIf(loginUser == null, ResponseCode.NOT_LOGIN_ERROR);
        Picture picture = pictureService.getById(pictureReviewRequest.getPictureId());
        ThrowUtil.throwIf(picture == null, ResponseCode.NOT_FOUND_ERROR);
        // 修改图片审核状态
        pictureService.setReviewStatue(picture,
                pictureReviewRequest.getReviewStatus(),
                picture.getReviewMessage(),
                loginUser.getId());
        // 保存审核信息
        Picture updatePicture = new Picture();
        updatePicture.setId(picture.getId());
        updatePicture.setReviewStatus(pictureReviewRequest.getReviewStatus());
        updatePicture.setReviewMessage(pictureReviewRequest.getReviewMessage());
        updatePicture.setReviewerId(loginUser.getId());
        pictureService.updateById(updatePicture);
        // 返回
        return ResultUtil.success(true);
    }



}
