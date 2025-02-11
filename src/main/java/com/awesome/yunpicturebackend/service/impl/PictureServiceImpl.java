package com.awesome.yunpicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.awesome.yunpicturebackend.annotation.AuthCheck;
import com.awesome.yunpicturebackend.common.ResponseCode;
import com.awesome.yunpicturebackend.common.utils.ResultUtil;
import com.awesome.yunpicturebackend.config.CosClientConfig;
import com.awesome.yunpicturebackend.exception.BusinessException;
import com.awesome.yunpicturebackend.exception.ThrowUtil;
import com.awesome.yunpicturebackend.manager.upload.PictureFileUpload;
import com.awesome.yunpicturebackend.manager.upload.PictureUrlUpload;
import com.awesome.yunpicturebackend.mapper.PictureMapper;
import com.awesome.yunpicturebackend.model.dto.file.UploadPictureResult;
import com.awesome.yunpicturebackend.model.dto.picture.*;
import com.awesome.yunpicturebackend.model.entity.Picture;
import com.awesome.yunpicturebackend.model.entity.Space;
import com.awesome.yunpicturebackend.model.entity.User;
import com.awesome.yunpicturebackend.model.enums.SortOrderEnum;
import com.awesome.yunpicturebackend.model.enums.TimePeriodEnum;
import com.awesome.yunpicturebackend.model.enums.UserRoleEnum;
import com.awesome.yunpicturebackend.model.vo.picture.PictureVO;
import com.awesome.yunpicturebackend.service.PictureService;
import com.awesome.yunpicturebackend.service.SpaceService;
import com.awesome.yunpicturebackend.service.UserService;
import com.awesome.yunpicturebackend.util.ColorSimilarityUtil;
import com.awesome.yunpicturebackend.util.StringUtil;
import com.awesome.yunpicturebackend.util.ValidateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.exception.MultiObjectDeleteException;
import com.qcloud.cos.model.DeleteObjectsRequest;
import com.qcloud.cos.model.DeleteObjectsResult;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
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

    @Resource
    private COSClient cosClient;

    @Resource
    private CosClientConfig cosClientConfig;

    // todo 为什么需要解决循环依赖？
    @Resource
    private SpaceService spaceService;

    @Resource
    private TransactionTemplate transactionTemplate;

    /**
     * 图片上传
     *
     * @param inputSource             文件源
     * @param loginUser               登录用户
     * @param pictureUploadRequest 自定义图片信息
     * @return 脱敏图片信息
     */
    @Override
    public PictureVO uploadPicture(Object inputSource, User loginUser, PictureUploadRequest pictureUploadRequest) {
        // 1.数据校验
        ThrowUtil.throwIf(inputSource == null, ResponseCode.NOT_LOGIN_ERROR, "图片不存在，上传错误");
        ThrowUtil.throwIf(loginUser == null, ResponseCode.NOT_LOGIN_ERROR, "用户未登录，图片上传错误");
        // 校验空间限额
        Long spaceId = pictureUploadRequest.getSpaceId();
        if (spaceId != null || spaceId > 0L) {
            spaceService.checkSpaceQuota(spaceId);
        } else {
            spaceId = 0L;
        }
        // 2.图片上传
        // 指定用户上传文件
        String uploadPathPrefix = "/public";
        // 如果是空间内上传，则存储到space目录下
        if (!ValidateUtil.isNullOrNotPositive(pictureUploadRequest.getSpaceId())){
            uploadPathPrefix = "/space/" + pictureUploadRequest.getSpaceId();
        }
        String userUpdatePathPrefix = uploadPathPrefix + '/' + loginUser.getId();
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
        Long pictureId = pictureUploadRequest.getPictureId();

        // 更新
        if (!ValidateUtil.isNullOrNotPositive(pictureId)) {
            Picture picture = this.getById(pictureId);
            String oldUrl = picture.getUrl();
            String oldCompressUrl = picture.getCompressUrl();
            // 更新后的数据
            picture.setUrl(uploadPictureResult.getUrl());
            picture.setCompressUrl(uploadPictureResult.getCompressUrl());
            picture.setPicSize(uploadPictureResult.getPicSize());
            picture.setPicWidth(uploadPictureResult.getPicWidth());
            picture.setPicHeight(uploadPictureResult.getPicHeight());
            picture.setPicScale(uploadPictureResult.getPicScale());
            picture.setPicFormat(uploadPictureResult.getPicFormat());
            picture.setPicColor(uploadPictureResult.getPicColor());
            // 使用事务
            transactionTemplate.execute( status -> {
                // 保存图片
                boolean pictureSaveResult = this.updateById(picture);
                ThrowUtil.throwIf(!pictureSaveResult, ResponseCode.OPERATION_ERROR, "图片保存失败");
                // 删除COS中的旧图片
                // 原图url
                if (StrUtil.isNotBlank(oldUrl)) {
                    String oldPictureUrlKey = StringUtil.getTruncatedString(oldUrl, cosClientConfig.getHost() + "/");
                    cosClient.deleteObject(cosClientConfig.getBucket(), oldPictureUrlKey);
                }
                // 压缩图url
                if (StrUtil.isNotBlank(oldCompressUrl)) {
                    String oldPictureCompressUrlKey = StringUtil.getTruncatedString(oldCompressUrl, cosClientConfig.getHost() + "/");
                    cosClient.deleteObject(cosClientConfig.getBucket(), oldPictureCompressUrlKey);
                }
                return picture;
            });
            return this.getPictureVO(picture);
        } else {
            Picture picture = new Picture();
            picture.setName(uploadPictureResult.getName());
            picture.setUserId(loginUser.getId());
            picture.setSpaceId(spaceId);
            // 若指定了空间Id，则图片默认为不公开的
            if (spaceId > 0){
                picture.setDoPub(0);
            } else {
                picture.setDoPub(1);
            }
            picture.setUrl(uploadPictureResult.getUrl());
            picture.setCompressUrl(uploadPictureResult.getCompressUrl());
            picture.setPicSize(uploadPictureResult.getPicSize());
            picture.setPicWidth(uploadPictureResult.getPicWidth());
            picture.setPicHeight(uploadPictureResult.getPicHeight());
            picture.setPicScale(uploadPictureResult.getPicScale());
            picture.setPicFormat(uploadPictureResult.getPicFormat());
            picture.setPicColor(uploadPictureResult.getPicColor());
            picture.setUploadSource(uploadSource.toString());
            // 存在自定义图片信息，则选择自定义信息
            if (pictureUploadRequest != null) {
                String pictureName = pictureUploadRequest.getPictureName();
                String category = pictureUploadRequest.getCategory();
                List<String> tagList = pictureUploadRequest.getTagList();
                if (StrUtil.isNotBlank(pictureName)) {
                    picture.setName(pictureName);
                }
                if (StrUtil.isNotBlank(category)) {
                    picture.setCategory(category);
                }
                if (CollUtil.isNotEmpty(tagList)) {
                    picture.setTags(JSONUtil.toJsonStr(tagList));
                }
            } else {
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
            // 使用事务
            Long finalSpaceId = spaceId;
            transactionTemplate.execute( status -> {
                // 保存图片
                boolean pictureSaveResult = this.save(picture);
                ThrowUtil.throwIf(!pictureSaveResult, ResponseCode.OPERATION_ERROR, "图片保存失败");
                // 更新空间
                if (finalSpaceId > 0L) {
                    boolean spaceUpdateResult = spaceService.lambdaUpdate()
                            .eq(Space::getId, finalSpaceId)
                            .setSql("totalSize = totalSize + " + picture.getPicSize())
                            .setSql("totalCount = totalCount + 1")
                            .update();
                    ThrowUtil.throwIf(!spaceUpdateResult, ResponseCode.OPERATION_ERROR,"空间额度更新失败");
                }
                return picture;
            });
            return this.getPictureVO(picture);
        }



    }

    /**
     * 批量爬取并导入图片（管理员用）
     *
     * @param pictureUploadByBatchRequest 批量上传请求参数
     * @param loginUser                   登录用户
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
                PictureUploadRequest pictureUploadRequest = new PictureUploadRequest();
                if (StrUtil.isNotBlank(pictureName)) {
                    pictureUploadRequest.setPictureName(pictureName + (uploadCount + 1));
                } else {
                    pictureUploadRequest.setPictureName(searchText + (uploadCount + 1));
                }
                if (StrUtil.isNotBlank(category)) {
                    pictureUploadRequest.setCategory(category);
                }
                if (CollUtil.isNotEmpty(tagList)) {
                    pictureUploadRequest.setTagList(tagList);
                }
                //endregion
                // 上传
                PictureVO pictureVO = this.uploadPicture(fileUrl, loginUser, pictureUploadRequest);
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

    // todo 进入图片详细页面获取质量高的图片
    public String getImgUrl(String linkUrl) {
        // 要抓取的地址
        String fetchUrl = String.format("https://www.bing.com%s", linkUrl);
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
     * @param pictureAdminQueryRequest 查询条件
     * @return 图片列表
     */
    @Override
    public List<Picture> getPictureList(PictureAdminQueryRequest pictureAdminQueryRequest) {
        List<Picture> pictureList = new ArrayList<>();
        if (pictureAdminQueryRequest != null) {
            QueryWrapper<Picture> queryWrapper = getQueryWrapper(pictureAdminQueryRequest);
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

    @Override
    public List<PictureVO> listPersonalPictureVO(PicturePersonalQueryRequest picturePersonalQueryRequest, boolean searchPicColor) {
        // 1.参数校验
        ThrowUtil.throwIf(picturePersonalQueryRequest == null , ResponseCode.PARAMS_ERROR);
        // 一定要空间数据
        Long spaceId = picturePersonalQueryRequest.getSpaceId();
        ThrowUtil.throwIf( spaceId == null || spaceId == 0 , ResponseCode.PARAMS_ERROR, "当前空间不存在");
        List<PictureVO> pictureVOList = new ArrayList<>();
        // 2.查询出当前空间图片
        QueryWrapper<Picture> queryWrapper = this.getQueryWrapper(picturePersonalQueryRequest);
        List<Picture> pictureList = this.list(queryWrapper);
        if (pictureList.isEmpty()) {
            return pictureVOList;
        }
        // 3.若需要查询相似颜色，则计算并筛选相似度大于等于0.80的数据
        // 查找颜色相似度
        // todo 可以优先通过前端指定颜色的阈值过滤掉一批颜色数据，提高效率
        List<Picture> similarColorPictureList = new ArrayList<>();
        if (searchPicColor){
            // 计算并筛选颜色相似度
            similarColorPictureList = pictureList.stream().filter(picture -> {
                String picColor = picture.getPicColor();
                // 没有主色调直接跳过
                if (StrUtil.isBlank(picColor)){
                    return false;
                }
                // 计算相似度
                double similarity = ColorSimilarityUtil.calculateSimilarity(picColor, picturePersonalQueryRequest.getPicColor());
                // 筛选出色彩相似度大于等于0.8的图片
                if (similarity >= 0.8) {
                    return true;
                }
                return false;
            }).collect(Collectors.toList());
        }
        // 4.将Picture转化为PictureVO
        if (searchPicColor){
            pictureVOList = similarColorPictureList.stream().map(PictureVO::objToVO).collect(Collectors.toList());
        } else {
            pictureVOList = pictureList.stream().map(PictureVO::objToVO).collect(Collectors.toList());
        }
        // 5.返回
        return pictureVOList;
    }

    /**
     * 分批获取脱敏图片列表（用于未登录用户）
     *
     * @param pictureQueryRequest 查询请求对象
     * @return 图片列表
     */
    @Override
    public List<PictureVO> listPictureVOBatch(PictureQueryRequest pictureQueryRequest) {
        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
        Page<Picture> picturePage = this.page(new Page<>(current, pageSize), getQueryWrapper(pictureQueryRequest));
        return this.getPictureVOList(picturePage.getRecords());
    }

    /**
     * 分批获取推荐脱敏图片列表（用于登录用户）
     *
     * @param pictureQueryRequest 查询请求对象
     * @return 图片列表
     */
    @AuthCheck(mustRole = "user")
    @Override
    public List<PictureVO> listRecommendPictureVOBatch(PictureQueryRequest pictureQueryRequest) {
        int current = pictureQueryRequest.getCurrent();
        int pageSize = pictureQueryRequest.getPageSize();
        // todo 结合用户的搜索偏好返回数据
        Page<Picture> picturePage = this.page(new Page<>(current, pageSize), getQueryWrapper(pictureQueryRequest));
        return this.getPictureVOList(picturePage.getRecords());
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest) {
        String searchText = pictureQueryRequest.getSearchText();
        String category = pictureQueryRequest.getCategory();
        List<String> tags = pictureQueryRequest.getTags();
        String sortField = pictureQueryRequest.getSortField();
        String sortOrder = pictureQueryRequest.getSortOrder();

        QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
        pictureQueryWrapper.and(StrUtil.isNotBlank(searchText),
                wrapper ->
                        wrapper.or(qw -> qw.like("name", searchText))
                                .or(qw -> qw.like("introduction", searchText))
                                .or(qw -> qw.like("tags", searchText))
                                .or(qw -> qw.like("category", searchText))
        );

        pictureQueryWrapper.eq(StrUtil.isNotBlank(category),"category", category);

        if (tags != null && !tags.isEmpty()){
            tags.forEach(tag -> {
                pictureQueryWrapper.like("tags", "\"" + tag + "\"");
            });
        }

        // 只能查看公共图库或者私有图库公开的图片
        pictureQueryWrapper.and(wrapper ->
            wrapper.or(qw -> qw.eq("spaceId", 0))
                    .or(qw -> qw.eq("doPub",1))
        );
        // 且必须为审核通过的图片
        pictureQueryWrapper.eq("reviewStatus", 1);

        return pictureQueryWrapper;
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PictureAdminQueryRequest pictureAdminQueryRequest) {
        Long id = pictureAdminQueryRequest.getId();
        String name = pictureAdminQueryRequest.getName();
        String category = pictureAdminQueryRequest.getCategory();
        List<String> tagList = pictureAdminQueryRequest.getTags();
        String searchText = pictureAdminQueryRequest.getSearchText();
        List<Integer> reviewStatus = pictureAdminQueryRequest.getReviewStatus();
        Long userId = pictureAdminQueryRequest.getUserId();
        Long spaceId = pictureAdminQueryRequest.getSpaceId();
        Integer doPub = pictureAdminQueryRequest.getDoPub();
        String sortField = pictureAdminQueryRequest.getSortField();
        String sortOrder = pictureAdminQueryRequest.getSortOrder();

        QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
        pictureQueryWrapper.like(id != null && id > 0, "id", id);
        pictureQueryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        pictureQueryWrapper.like(StrUtil.isNotBlank(category), "category", category);
        pictureQueryWrapper.and(StrUtil.isNotBlank(searchText),
                q -> q.or(
                        i -> i.like("name", searchText).or().like("introduction", searchText).or().like("tags", searchText)
                )
        );
        // 审核状态查询
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
        pictureQueryWrapper.eq(!ValidateUtil.isNullOrNotPositive(spaceId) ,"spaceId", spaceId);
        pictureQueryWrapper.eq(doPub != null && doPub >= 0,"doPub", doPub);
        pictureQueryWrapper.orderBy(StrUtil.isNotBlank(sortField), SortOrderEnum.ASC.getValue().equals(sortOrder), sortField);

        return pictureQueryWrapper;
    }

    @Override
    public QueryWrapper<Picture> getQueryWrapper(PicturePersonalQueryRequest picturePersonalQueryRequest) {
        ThrowUtil.throwIf(picturePersonalQueryRequest == null , ResponseCode.PARAMS_ERROR);
        Long id = picturePersonalQueryRequest.getId();
        String name = picturePersonalQueryRequest.getName();
        String category = picturePersonalQueryRequest.getCategory();
        List<String> tags = picturePersonalQueryRequest.getTags();
        String picFormat = picturePersonalQueryRequest.getPicFormat();
        String picColor = picturePersonalQueryRequest.getPicColor();
        String searchText = picturePersonalQueryRequest.getSearchText();
        List<Integer> reviewStatus = picturePersonalQueryRequest.getReviewStatus();
        String timePeriod = picturePersonalQueryRequest.getTimePeriod();
        Long spaceId = picturePersonalQueryRequest.getSpaceId();
        Integer doPub = picturePersonalQueryRequest.getDoPub();
        String sortField = picturePersonalQueryRequest.getSortField();
        String sortOrder = picturePersonalQueryRequest.getSortOrder();

        QueryWrapper<Picture> pictureQueryWrapper = new QueryWrapper<>();
        // 必须指定空间
        if (spaceId != null && spaceId > 0){
            pictureQueryWrapper.eq("spaceId", spaceId);
        } else {
           throw new BusinessException(ResponseCode.NOT_FOUND_ERROR, "空间数据异常");
        }
        pictureQueryWrapper.like(id != null && id > 0, "id", id);
        pictureQueryWrapper.like(StrUtil.isNotBlank(name), "name", name);
        pictureQueryWrapper.like(StrUtil.isNotBlank(category), "category", category);
        // todo 使用ES分词搜索图片名称和简介
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
        if (CollUtil.isNotEmpty(tags)) {
            tags.forEach(tag -> {
                pictureQueryWrapper.like("tags", "\"" + tag + "\"");
            });
        }
        pictureQueryWrapper.eq(StrUtil.isNotBlank(picFormat),"pictureFormat", picFormat);

        // 当需要查询色彩相似度时，先过滤缺失相似度的数据
        pictureQueryWrapper.isNotNull(StrUtil.isNotBlank(picColor), "picColor");

        // 查询图片时间段
        if (StrUtil.isNotBlank(timePeriod)) {
            // 获取当前时间
            LocalDateTime now = LocalDateTime.now();
            TimePeriodEnum timePeriodEnum = TimePeriodEnum.getEnumByValue(timePeriod);
            // 根据不同时间段设置查询条件
            switch (timePeriodEnum) {
                case INANHOUR:
                    LocalDateTime inOneHour = now.minusHours(1);
                    pictureQueryWrapper.ge("createTime", inOneHour);
                    break;
                case INPASSTFHOURS:
                    LocalDateTime inPass24Hours = now.minusDays(1);
                    pictureQueryWrapper.ge("createTime", inPass24Hours);
                    break;
                case INPASSWEEK:
                    LocalDateTime inPassWeek = now.minusWeeks(1);
                    pictureQueryWrapper.ge("createTime", inPassWeek);
                case INPASSMONTH:
                    LocalDateTime inPassMonth = now.minusMonths(1);
                    pictureQueryWrapper.ge("timePeriod", timePeriod);
                    break;
            }
        }

        pictureQueryWrapper.eq(doPub != null && doPub >= 0,"doPub", doPub);

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
            // 更新图片
            if (picture.getReviewStatus() != null && picture.getReviewStatus() > 0){
                // 原本存在审核信息,则表示更新图片，重新设置审核信息
                    picture.setReviewMessage("");
            }else {
                // 新增图片
                picture.setReviewStatus(0);
            }
            // 根据传参填写
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

    /**
     * 删除图片对象
     *
     * @param pictureObject   图片（Id或者对象）
     * @param deleteCosObject 是否同时删除对象存储内容
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, CosServiceException.class, CosClientException.class})
    public boolean deletePicture(Object pictureObject, boolean deleteCosObject) {
        // 1.校验参数
        if (pictureObject == null) {
            log.info("pictureObject 参数错误");
            return false;
        }
        // 2.辨别参数类型，并且获取操作的图片对象
        Picture picture = null;
        if (pictureObject instanceof Picture) {
            picture = (Picture) pictureObject;
        } else if (pictureObject instanceof Long) {
            picture = getById((Long) pictureObject);
            if (picture == null) {
                log.info("图片Id：" + pictureObject + "，不存在");
                return false;
            }
        } else {
            log.info("图片数据 pictureObject 错误：");
            return false;
        }
        // 3.删除数据
        try {
            // 删除数据库数据
            this.removeById(picture.getId());
            // 删除对象存储数据
            if (deleteCosObject) {
                // 原图url
                String originUrl = picture.getUrl();
                if (StrUtil.isNotBlank(originUrl)) {
                    String pictureUrlKey = StringUtil.getTruncatedString(originUrl, cosClientConfig.getHost() + "/");
                    cosClient.deleteObject(cosClientConfig.getBucket(), pictureUrlKey);
                }
                // 压缩图url
                String compressUrl = picture.getCompressUrl();
                if (StrUtil.isNotBlank(compressUrl)) {
                    String pictureCompressUrlKey = StringUtil.getTruncatedString(compressUrl, cosClientConfig.getHost() + "/");
                    cosClient.deleteObject(cosClientConfig.getBucket(), pictureCompressUrlKey);
                }
            }
            // 更新空间限额
            // 更新空间
            Long pictureSpaceId = picture.getSpaceId();
            if (pictureSpaceId > 0L) {
                // todo 当前限额数值为0时需要判断吗？
                boolean spaceUpdateResult = spaceService.lambdaUpdate()
                        .eq(Space::getId, pictureSpaceId)
                        .setSql("totalSize = totalSize - " + picture.getPicSize())
                        .setSql("totalCount = totalCount - 1")
                        .update();
                ThrowUtil.throwIf(!spaceUpdateResult, ResponseCode.OPERATION_ERROR,"空间额度更新失败");
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 批量删除图片
     * @param pictureIdList 图片Id集合
     * @param deleteCosObject 是否同时删除对象存储内容
     * @return
     */
    @Override
    @Transactional(rollbackFor = CosClientException.class)
    public boolean deletePictureByIds(List<Long> pictureIdList, boolean deleteCosObject) {
        // 1.校验参数
        if (pictureIdList == null || pictureIdList.isEmpty()) {
            log.info("图片Id列表为空");
            return false;
        }
        // 2.删除数据
        try {
            // 获取图片列表
            List<Picture> pictureList = this.listByIds(pictureIdList);
            if (CollUtil.isEmpty(pictureList)) {
                log.info("查询的图片列表为空");
                return false;
            }
            // 删除数据库
            this.removeByIds(pictureIdList);
            // 删除对象存储
            if (deleteCosObject) {
                try {
                    deleteCosPictures(pictureList);
                } catch (CosClientException cce) {
                    throw cce;
                }
            }
        } catch (RuntimeException e) {
            throw e;
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = CosClientException.class)
    public boolean deletePictureByPictureList(List<Picture> pictureList, boolean deleteCosObject) {
        // 1.校验参数
        if (pictureList == null || pictureList.isEmpty()) {
            log.info("图片列表为空");
            return false;
        }
        // 2.删除数据
        try {
            // 删除数据库
            this.removeByIds(pictureList);
            // 删除对象存储
            if (deleteCosObject) {
                try {
                    deleteCosPictures(pictureList);
                } catch (CosClientException cce) {
                    throw cce;
                }
            }
        } catch (RuntimeException e) {
            throw e;
        }
        return true;
    }

    /**
     * 批量删除对象存储中的图片
     *
     * @param pictureList 图片列表
     */
    @Override
    @Async
    public void deleteCosPictures(List<Picture> pictureList) {
        // 删除对象存储数据
        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(cosClientConfig.getBucket());
        // 设置要删除的key列表, 最多一次删除1000个
        // 原图keys
        ArrayList<DeleteObjectsRequest.KeyVersion> sourceKeyList = new ArrayList<>();
        // 压缩图keys
        ArrayList<DeleteObjectsRequest.KeyVersion> compressKeyList = new ArrayList<>();
        // 遍历图片列表获取Cos key
        pictureList.forEach(picture -> {
            // 获取原图url
            if (StrUtil.isNotBlank(picture.getUrl())) {
                sourceKeyList.add(
                        new DeleteObjectsRequest.KeyVersion(
                                StringUtil.getTruncatedString(picture.getUrl(), cosClientConfig.getHost() + "/")
                        )
                );
            }
            // 获取压缩图url
            if (StrUtil.isNotBlank(picture.getCompressUrl())) {
                compressKeyList.add(
                        new DeleteObjectsRequest.KeyVersion(
                                StringUtil.getTruncatedString(picture.getCompressUrl(), cosClientConfig.getHost() + "/")
                        )
                );
            }
        });
        try {
            // 删除原图
            try {
                deleteObjectsRequest.setKeys(sourceKeyList);
                DeleteObjectsResult deleteSourceResult = cosClient.deleteObjects(deleteObjectsRequest);
            } catch (MultiObjectDeleteException mde) {
                // 如果部分删除成功部分失败, 返回 MultiObjectDeleteException
                List<MultiObjectDeleteException.DeleteError> deleteErrors = mde.getErrors();
                log.info("原图部分删除失败：" + deleteErrors);
                throw mde;
            }
            // 删除压缩图
            try {
                deleteObjectsRequest.setKeys(compressKeyList);
                DeleteObjectsResult deleteCompressResult = cosClient.deleteObjects(deleteObjectsRequest);
            } catch (MultiObjectDeleteException mde) {
                // 如果部分删除成功部分失败, 返回 MultiObjectDeleteException
                List<MultiObjectDeleteException.DeleteError> deleteErrors = mde.getErrors();
                log.info("压缩图部分删除失败：" + deleteErrors);
                throw mde;
            }
        } catch (CosServiceException e) {
            log.error("CosServiceException:" + e.getMessage());
            throw e;
        } catch (CosClientException e) {
            log.error("CosClientException:" + e.getMessage());
            throw e;
        }
    }

}
