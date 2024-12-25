package com.awesome.yunpicturebackend.manager;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.awesome.yunpicturebackend.common.ResponseCode;
import com.awesome.yunpicturebackend.common.utils.ResultUtil;
import com.awesome.yunpicturebackend.config.CosClientConfig;
import com.awesome.yunpicturebackend.exception.BusinessException;
import com.awesome.yunpicturebackend.exception.ThrowUtil;
import com.awesome.yunpicturebackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.UploadResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.OriginalInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 可复用的cos操作类
 */
@Service
@Slf4j
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 将图片文件上传到 COS
     * @param multipartFile 文件流
     * @param uploadPathPrefix 上传地址前缀（父目录）
     */
    public UploadPictureResult uploadPictureObject(MultipartFile multipartFile,String uploadPathPrefix) {
        // 1.校验图片
        validPicture(multipartFile);
        // 2.自定义图片名称和上传地址
        String uuid = RandomUtil.randomString(8);
        String originalFilename = multipartFile.getOriginalFilename();
        String uploadFileName = String.format("%s_%s.%s", System.currentTimeMillis(), uuid, FileUtil.extName(originalFilename));
        String uploadPath = String.format("%s/%s", uploadPathPrefix, uploadFileName);
        // 3.上传文件
        // 3.1 创建一个临时文件
        File tempFile = null;
        try {
            // 3.2将上传的文件保存到服务器的临时文件中
            tempFile = File.createTempFile(uploadPath, null);
            multipartFile.transferTo(tempFile);
            // 3.3 将临时文件中的内容保存到cos
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, tempFile);
            // 4.封装返回的上传结果
            // Cos返回的图片信息对象
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            uploadPictureResult.setUrl(cosClientConfig.getHost() + uploadPath);
            uploadPictureResult.setName(FileUtil.mainName(originalFilename));
            uploadPictureResult.setPicSize(FileUtil.size(tempFile));
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(imageInfo.getFormat());
            return uploadPictureResult;
        } catch (CosServiceException e) {
            log.error(e.getMessage());
            throw new BusinessException(ResponseCode.OPERATION_ERROR,"文件上传失败");
        } catch (IOException e) {
            log.error("file upload error; filePath = " + uploadPath);
            throw new BusinessException(ResponseCode.OPERATION_ERROR,"文件上传失败");
        } finally {
            // 4.删除临时文件
            deleteTempFile(tempFile);
        }
    }

    /**
     * 删除临时文件
     * @param tempFile 临时文件
     */
    public void deleteTempFile(File tempFile) {
        if (tempFile != null) {
            boolean deleteResult = tempFile.delete();
            if (!deleteResult) {
                log.error("tempFile delete error; filePath = " + tempFile.getPath());
            }
        }
    }

    /**
     * 校验图片
     * @param multipartFile 文件数据流
     */
    private void validPicture(MultipartFile multipartFile) {
        ThrowUtil.throwIf(multipartFile == null, ResponseCode.PARAMS_ERROR,"上传的图片文件不存在");
        // 校验大小
        long pictureSize = multipartFile.getSize();
        final long ONE_MB = 1024 * 1024L;
        ThrowUtil.throwIf(pictureSize > ONE_MB, ResponseCode.PARAMS_ERROR, "图片大小不能超过1MB");
        // 校验文件后缀
        String pictureSuffixName = FileUtil.extName(multipartFile.getOriginalFilename());
        List<String> ALLOW_SUFFIX = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
        ThrowUtil.throwIf(!ALLOW_SUFFIX.contains(pictureSuffixName), ResponseCode.PARAMS_ERROR, "文件类型错误");
    }


}
