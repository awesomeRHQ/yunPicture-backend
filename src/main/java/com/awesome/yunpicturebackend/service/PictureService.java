package com.awesome.yunpicturebackend.service;


import com.awesome.yunpicturebackend.model.dto.picture.*;
import com.awesome.yunpicturebackend.model.entity.Picture;
import com.awesome.yunpicturebackend.model.entity.User;
import com.awesome.yunpicturebackend.model.vo.picture.PictureVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 针对表【picture(图片表)】的服务Service
 *
 * @author awesomeRHQ
 * @since 2024-12-23 16:23:58
 */
public interface PictureService extends IService<Picture> {

    /**
     * 图片上传
     * @param inputSource 图片输入源
     * @param loginUser 登录用户
     * @param pictureUploadRequest 图片信息
     * @return 脱敏图片信息
     */
    PictureVO uploadPicture(Object inputSource, User loginUser, PictureUploadRequest pictureUploadRequest);

    /**
     * 批量爬取并导入图片（管理员用）
     * @param pictureUploadByBatchRequest 批量上传请求参数
     * @param loginUser 登录用户
     * @return 上传成功图片数量
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser);

    /**
     * 图片信息脱敏
     * @param picture 图片信息
     * @return 脱敏后的图片信息
     */
    PictureVO getPictureVO(Picture picture);

    /**
     * 获取图片列表
     * @param pictureAdminQueryRequest 查询条件
     * @return 图片列表
     */
    List<Picture> getPictureList(PictureAdminQueryRequest pictureAdminQueryRequest);

    /**
     * 获取脱敏图片分页列表
     * @param pictureList 图片列表
     * @return 图片分页列表
     */
    List<PictureVO> getPictureVOList(List<Picture> pictureList);

    /**
     * 获取脱敏图片分页列表
     * @param picturePage 图片分页列表
     * @return 脱敏图片分页列表
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage);

    /**
     * 获取个人图库图片列表
     * @param picturePersonalQueryRequest
     * @param searchPicColor 是否需要过滤颜色
     * @return
     */
    List<PictureVO> listPersonalPictureVO(PicturePersonalQueryRequest picturePersonalQueryRequest, boolean searchPicColor);

    /**
     * 分批获取脱敏图片列表（用于未登录用户）
     * @param pictureQueryRequest 查询请求对象
     * @return 图片列表
     */
    List<PictureVO> listPictureVOBatch(PictureQueryRequest pictureQueryRequest);

    /**
     * 分批获取推荐脱敏图片列表（用于登录用户）
     * @param pictureQueryRequest 查询请求对象
     * @return 图片列表
     */
    List<PictureVO> listRecommendPictureVOBatch(PictureQueryRequest pictureQueryRequest);

    /**
     * 拼接查询条件
     * @param pictureQueryRequest 查询请求类
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 拼接查询条件
     * @param pictureAdminQueryRequest 管理员查询请求类
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureAdminQueryRequest pictureAdminQueryRequest);

    /**
     * 拼接查询条件
     * @param picturePersonalQueryRequest 用户个人查询请求类
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PicturePersonalQueryRequest picturePersonalQueryRequest);

    /**
     * 设置图片审核信息
     * @param picture 审核图片
     * @param reviewStatus 审核状态
     * @param reviewMessage 审核消息
     * @param reviewerId 审核人Id
     */
    void setReviewStatue(Picture picture, Integer reviewStatus, String reviewMessage, Long reviewerId);

    /**
     * 删除图片对象
     * @param pictureObject 图片（Id或者对象）
     * @param deleteCosObject 是否同时删除对象存储内容
     */
    boolean deletePicture(Object pictureObject,boolean deleteCosObject);

    /**
     * 批量删除图片对象
     * @param pictureIdList 图片Id集合
     * @param deleteCosObject 是否同时删除对象存储内容
     */
    boolean deletePictureByIds(List<Long> pictureIdList,boolean deleteCosObject);

    /**
     * 批量删除图片对象
     * @param pictureList 图片集合
     * @param deleteCosObject 是否同时删除对象存储内容
     */
    boolean deletePictureByPictureList(List<Picture> pictureList,boolean deleteCosObject);

    /**
     * 批量删除对象存储中的图片
     * @param pictureList 图片列表
     */
    void deleteCosPictures(List<Picture> pictureList);
}
