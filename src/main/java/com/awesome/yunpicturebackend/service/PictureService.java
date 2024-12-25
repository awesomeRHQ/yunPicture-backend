package com.awesome.yunpicturebackend.service;


import com.awesome.yunpicturebackend.model.dto.picture.PictureQueryRequest;
import com.awesome.yunpicturebackend.model.dto.picture.PictureUploadRequest;
import com.awesome.yunpicturebackend.model.entity.Picture;
import com.awesome.yunpicturebackend.model.entity.User;
import com.awesome.yunpicturebackend.model.vo.picture.PictureVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
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
     * @param multipartFile 文件流
     * @param pictureUploadRequest 图片id（鉴别添加删除）
     * @param loginUser 登录用户
     * @return 脱敏图片信息
     */
    PictureVO uploadPicture(MultipartFile multipartFile, PictureUploadRequest pictureUploadRequest, User loginUser);

    /**
     * 图片信息脱敏
     * @param picture 图片信息
     * @return 脱敏后的图片信息
     */
    PictureVO getPictureVO(Picture picture);

    /**
     * 获取图片列表
     * @param pictureQueryRequest 查询条件
     * @return 图片列表
     */
    List<Picture> getPictureList(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取脱敏图片列表
     * @param pictureList 图片列表
     * @return 图片列表
     */
    List<PictureVO> getPictureVOList(List<Picture> pictureList);

    /**
     * 获取脱敏图片分页列表
     * @param picturePage 图片分页列表
     * @return 脱敏图片分页列表
     */
    Page<PictureVO> getPictureVOPage(Page<Picture> picturePage);

    /**
     * 拼接查询条件
     * @param pictureQueryRequest 查询请求类
     * @return
     */
    QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

}
