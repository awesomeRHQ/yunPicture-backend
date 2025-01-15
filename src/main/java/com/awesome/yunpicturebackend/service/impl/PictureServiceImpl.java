package com.awesome.yunpicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.awesome.yunpicturebackend.annotation.AuthCheck;
import com.awesome.yunpicturebackend.common.ResponseCode;
import com.awesome.yunpicturebackend.exception.BusinessException;
import com.awesome.yunpicturebackend.exception.ThrowUtil;
import com.awesome.yunpicturebackend.manager.upload.PictureFileUpload;
import com.awesome.yunpicturebackend.manager.upload.PictureUrlUpload;
import com.awesome.yunpicturebackend.mapper.PictureMapper;
import com.awesome.yunpicturebackend.model.bo.picture.PictureUploadCustomInfo;
import com.awesome.yunpicturebackend.model.dto.file.UploadPictureResult;
import com.awesome.yunpicturebackend.model.dto.picture.PictureLoadMoreRequest;
import com.awesome.yunpicturebackend.model.dto.picture.PictureQueryRequest;
import com.awesome.yunpicturebackend.model.dto.picture.PictureUploadByBatchRequest;
import com.awesome.yunpicturebackend.model.entity.Picture;
import com.awesome.yunpicturebackend.model.entity.User;
import com.awesome.yunpicturebackend.model.enums.SortOrderEnum;
import com.awesome.yunpicturebackend.model.enums.UserRoleEnum;
import com.awesome.yunpicturebackend.model.vo.picture.PictureVO;
import com.awesome.yunpicturebackend.service.PictureService;
import com.awesome.yunpicturebackend.service.TagService;
import com.awesome.yunpicturebackend.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 针对表【picture(图片表)】的服务Service实现类
 *
 * @author awesomeRHQ
 * @since 2024-12-23 16:23:58
 */
@Slf4j
@Service("PictureService")
public class PictureServiceImpl extends ServiceImpl<PictureMapper, Picture> implements PictureService {

    @Resource
    private UserService userService;

    @Resource
    private PictureFileUpload pictureFileUpload;

    @Resource
    private PictureUrlUpload pictureUrlUpload;

    /**
     * 图片上传
     *
     * @param inputSource          文件源
     * @param loginUser            登录用户
     * @param pictureUploadCustomInfo            自定义图片信息
     * @return 脱敏图片信息
     */
    @Override
    public PictureVO uploadPicture(Object inputSource, User loginUser, PictureUploadCustomInfo pictureUploadCustomInfo) {
        // 1.数据校验
        ThrowUtil.throwIf(inputSource == null, ResponseCode.NOT_LOGIN_ERROR, "图片不存在，上传错误");
        ThrowUtil.throwIf(loginUser == null, ResponseCode.NOT_LOGIN_ERROR, "用户未登录，图片上传错误");
        // 2.图片上传
        // 2.1指定用户上传文件
        String publicPathPrefix = "/public";
        String userUpdatePathPrefix = publicPathPrefix + '/' + loginUser.getId();
        UploadPictureResult uploadPictureResult = null;
        StringBuilder uploadSource = new StringBuilder();
        if (inputSource instanceof MultipartFile) {
            uploadSource.append("用户本地上传");
            uploadPictureResult = pictureFileUpload.uploadPictureObject(inputSource, userUpdatePathPrefix);
        } else if (inputSource instanceof String) {
            uploadSource.append((String) inputSource);
            uploadPictureResult = pictureUrlUpload.uploadPictureObject(inputSource, userUpdatePathPrefix);
        } else {
            return null;
        }
        ThrowUtil.throwIf(uploadPictureResult == null, ResponseCode.SYSTEM_ERROR);
        // 3.处理结果
        Picture picture = new Picture();
        picture.setUserId(loginUser.getId());
        picture.setUrl(uploadPictureResult.getUrl());
        picture.setCompressUrl(uploadPictureResult.getCompressUrl());
        picture.setPicSize(uploadPictureResult.getPicSize());
        picture.setPicWidth(uploadPictureResult.getPicWidth());
        picture.setPicHeight(uploadPictureResult.getPicHeight());
        picture.setPicScale(uploadPictureResult.getPicScale());
        picture.setPicFormat(uploadPictureResult.getPicFormat());
        picture.setUploadSource(uploadSource.toString());
        // 存在自定义图片信息，则选择自定义信息
        if (pictureUploadCustomInfo != null) {
            String pictureName = pictureUploadCustomInfo.getPictureName();
            String category = pictureUploadCustomInfo.getCategory();
            List<String> tagList = pictureUploadCustomInfo.getTagList();
            if (StrUtil.isNotBlank(pictureName)) {
                picture.setName(pictureName);
            }
            if (StrUtil.isNotBlank(category)) {
                picture.setCategory(category);
            }
            if (CollUtil.isNotEmpty(tagList)) {
                picture.setTags(JSONUtil.toJsonStr(tagList));
            }
        }
        else{
            picture.setName(uploadPictureResult.getName());
        }
        // 设置审核状态
        // 若当前图片创建人为管理员
        if (UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole())) {
            // 则直接审核通过
            this.setReviewStatue(picture, 0, "", loginUser.getId());
        } else {
            // 否则走审核
            this.setReviewStatue(picture, 0, "", 0L);
        }
        boolean res = this.saveOrUpdate(picture);
        ThrowUtil.throwIf(!res, ResponseCode.SYSTEM_ERROR, "图片保存数据库错误");
        return this.getPictureVO(picture);
    }

    /**
     * 批量爬取并导入图片（管理员用）
     * @param pictureUploadByBatchRequest 批量上传请求参数
     * @param loginUser 登录用户
     * @return 上传成功图片数量
     */
    @Override
    public Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser) {
        String searchText = pictureUploadByBatchRequest.getSearchText();
        // 格式化数量
        Integer count = pictureUploadByBatchRequest.getSearchCount();
        ThrowUtil.throwIf(count > 30, ResponseCode.PARAMS_ERROR, "最多 30 条");
        // 要抓取的地址
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取页面失败", e);
            throw new BusinessException(ResponseCode.OPERATION_ERROR, "获取页面失败");
        }
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjUtil.isNull(div)) {
            throw new BusinessException(ResponseCode.OPERATION_ERROR, "获取元素失败");
        }
        //Elements imgElementList = div.select("a.iusc"); 获取详细页链接
        Elements imgElementList = div.select("img.mimg");
        int uploadCount = 0;
        for (Element imgElement : imgElementList) {
            String fileUrl = imgElement.attr("src");
            if (StrUtil.isBlank(fileUrl)) {
                log.info("当前链接为空，已跳过: {}", fileUrl);
                continue;
            }
            // 处理图片上传地址，防止出现转义问题
            int questionMarkIndex = fileUrl.indexOf("?");
            if (questionMarkIndex > -1) {
                fileUrl = fileUrl.substring(0, questionMarkIndex);
            }
            // 上传图片
            try {
                //region 设置自定义图片信息
                String pictureName = pictureUploadByBatchRequest.getPictureName();
                String category = pictureUploadByBatchRequest.getCategory();
                List<String> tagList = pictureUploadByBatchRequest.getTagList();
                PictureUploadCustomInfo pictureUploadCustomInfo = new PictureUploadCustomInfo();
                if (StrUtil.isNotBlank(pictureName)){
                    pictureUploadCustomInfo.setPictureName(pictureName + (uploadCount+1));
                } else {
                    pictureUploadCustomInfo.setPictureName(searchText + (uploadCount+1));
                }
                if (StrUtil.isNotBlank(category)){
                    pictureUploadCustomInfo.setCategory(category);
                }
                if (CollUtil.isNotEmpty(tagList)){
                    pictureUploadCustomInfo.setTagList(tagList);
                }
                //endregion
                // 上传
                PictureVO pictureVO = this.uploadPicture(fileUrl, loginUser, pictureUploadCustomInfo);
                log.info("图片上传成功, id = {}", pictureVO.getId());
                uploadCount++;
            } catch (Exception e) {
                log.error("图片上传失败", e);
                continue;
            }
            if (uploadCount >= count) {
                break;
            }
        }
        return uploadCount;
    }

    // todo 进入详细页面获取质量高的图片
    public String getImgUrl(String linkUrl){
        // 要抓取的地址
        String fetchUrl = String.format("https://www.bing.com%s",linkUrl);
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取页面失败", e);
            throw new BusinessException(ResponseCode.OPERATION_ERROR, "获取页面失败");
        }
        // 总是为null
        Element div = document.getElementsByClass("imgContainer").first();
        if (ObjUtil.isNull(div)) {
            throw new BusinessException(ResponseCode.OPERATION_ERROR, "获取元素失败");
        }
        Elements img = div.select("img");
        if (ObjUtil.isNull(img)) {
            throw new BusinessException(ResponseCode.OPERATION_ERROR, "获取元素失败");
        }
        return img.attr("src");
    }

    /**
     * 图片信息脱敏
     *
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
        pictureVO.setTags(JSONUtil.toList(picture.getTags(), String.class));
        if (picture.getUserId() > 0) {
            pictureVO.setUserVO(userService.getUserVO(picture.getUserId()));
        }
        return pictureVO;
    }

    /**
     * 获取图片列表
     *
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
     *
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
     *
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
     *
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
        // 限定普通用户只能查看审核通过的图片
        pictureQueryRequest.setReviewStatus(List.of(1));
        Page<Picture> picturePage = this.page(new Page<>(current, pageSize), getQueryWrapper(pictureQueryRequest));
        return this.getPictureVOList(picturePage.getRecords());
    }

    /**
     * 分批获取推荐脱敏图片列表（用于登录用户）
     *
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
        // 限定普通用户只能查看审核通过的图片
        pictureQueryRequest.setReviewStatus(List.of(1));
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
        List<Integer> reviewStatus = pictureQueryRequest.getReviewStatus();
        Long userId = pictureQueryRequest.getUserId();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();

        QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
        pictureQueryWrapper.like(id != null && id > 0, "id", id);
        pictureQueryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        pictureQueryWrapper.like(StrUtil.isNotBlank(introduction), "introduction", introduction);
        pictureQueryWrapper.like(StrUtil.isNotBlank(category), "category", category);
        pictureQueryWrapper.and(StrUtil.isNotBlank(searchText),
                q -> q.or(
                        i -> i.like("name", searchText).or().like("introduction", searchText).or().like("tags", searchText)
                )
        );
        if (CollUtil.isNotEmpty(reviewStatus)) {
            // sql: and (x or x)
            pictureQueryWrapper.and(wrapper -> {
                reviewStatus.forEach(status -> {
                    wrapper.or(r -> r.eq("reviewStatus", status));
                });
            });
        }
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


    /**
     * 设置图片审核信息
     *
     * @param picture       审核图片
     * @param reviewStatus  审核状态
     * @param reviewMessage 审核消息
     * @param reviewerId    审核人Id
     */
    @Override
    public void setReviewStatue(Picture picture, Integer reviewStatus, String reviewMessage, Long reviewerId) {
        ThrowUtil.throwIf(picture == null, ResponseCode.OPERATION_ERROR, "审核图片信息缺失");
        // 参数picture可能携带id但是不携带userId
        if (picture.getUserId() == null) {
            Picture existPicture = this.getById(picture.getId());
            ThrowUtil.throwIf(existPicture == null, ResponseCode.NOT_FOUND_ERROR, "图片信息不存在");
            picture.setUserId(existPicture.getUserId());
        }
        // 若审核人为图片创建人，则自动通过
        if (picture.getUserId().equals(reviewerId)) {
            picture.setReviewStatus(1);
            picture.setReviewMessage("管理员审核自动通过");
            picture.setReviewerId(reviewerId);
        } else {
            // 原本存在审核信息,则表示更新图片，重新设置审核信息
            if (picture.getReviewStatus() > 0) {
                picture.setReviewMessage("");
            }
            if (reviewStatus != null || reviewStatus > 0) {
                picture.setReviewStatus(reviewStatus);
            }
            if (StrUtil.isNotBlank(reviewMessage)) {
                picture.setReviewMessage(reviewMessage);
            }
            if (reviewerId != null && reviewerId > 0) {
                picture.setReviewerId(reviewerId);
            }
        }
    }

}
