package com.awesome.yunpicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.awesome.yunpicturebackend.common.ResponseCode;
import com.awesome.yunpicturebackend.config.CosClientConfig;
import com.awesome.yunpicturebackend.exception.BusinessException;
import com.awesome.yunpicturebackend.manager.CosManager;
import com.awesome.yunpicturebackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 将图片文件上传到 COS
     *
     * @param inputSource 图片输入源
     * @param uploadPathPrefix 上传地址前缀（父目录）
     */
    public UploadPictureResult uploadPictureObject(Object inputSource, String uploadPathPrefix) {
        // 1.校验图片
        validPicture(inputSource);
        // 2.自定义图片名称和上传地址
        String uuid = RandomUtil.randomString(8);
        String originalFilename = getOriginalFilename(inputSource);
        String uploadFileName = String.format("%s_%s.%s", System.currentTimeMillis(), uuid, FileUtil.extName(originalFilename));
        String uploadPath = String.format("%s/%s", uploadPathPrefix, uploadFileName);
        // 3.上传文件
        // 3.1 创建一个临时文件
        File tempFile = null;
        try {
            // 3.2将上传的文件保存到服务器的临时文件中
            tempFile = File.createTempFile(uploadPath, null);
            processPictureFile(inputSource,tempFile);
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
            throw new BusinessException(ResponseCode.OPERATION_ERROR, "文件上传失败");
        } catch (IOException e) {
            log.error("file upload error; filePath = " + uploadPath);
            throw new BusinessException(ResponseCode.OPERATION_ERROR, "文件上传失败");
        } finally {
            // 5.删除临时文件
            deleteTempFile(tempFile);
        }
    }

    /**
     * 校验图片信息
     * @param inputSource 文件源
     */
    protected abstract void validPicture(Object inputSource);

    /**
     * 获取源文件名称
     * @param inputSource 文件源
     * @return 文件名称
     */
    protected abstract String getOriginalFilename(Object inputSource);

    /**
     * 将图片暂存到本地
     * @param inputSource 文件源
     * @param tempFile 本地临时文件夹
     */
    protected abstract void processPictureFile(Object inputSource, File tempFile);

    /**
     * 删除临时文件
     *
     * @param tempFile 临时文件
     */
    private void deleteTempFile(File tempFile) {
        if (tempFile != null) {
            boolean deleteResult = tempFile.delete();
            if (!deleteResult) {
                log.error("tempFile delete error; filePath = " + tempFile.getPath());
            }
        }
    }
}
