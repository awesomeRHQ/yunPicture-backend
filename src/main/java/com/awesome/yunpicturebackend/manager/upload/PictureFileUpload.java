package com.awesome.yunpicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.awesome.yunpicturebackend.common.ResponseCode;
import com.awesome.yunpicturebackend.exception.ThrowUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 图片文件上传
 */
@Slf4j
@Service
public class PictureFileUpload extends PictureUploadTemplate{

    @Override
    protected void validPicture(Object inputSource) {
        ThrowUtil.throwIf(inputSource == null, ResponseCode.PARAMS_ERROR, "上传的图片文件不存在");
        MultipartFile multipartFile = (MultipartFile) inputSource;
        // 校验大小
        long pictureSize = multipartFile.getSize();
        final long SIX_MB = 6 * 1024 * 1024L;
        ThrowUtil.throwIf(pictureSize > SIX_MB, ResponseCode.PARAMS_ERROR, "图片大小不能超过6MB");
        // 校验文件后缀
        String pictureSuffixName = FileUtil.extName(multipartFile.getOriginalFilename());
        List<String> ALLOW_SUFFIX = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
        ThrowUtil.throwIf(!ALLOW_SUFFIX.contains(pictureSuffixName), ResponseCode.PARAMS_ERROR, "文件类型错误");
    }

    @Override
    protected String getOriginalFilename(Object inputSource) {
        ThrowUtil.throwIf(inputSource == null, ResponseCode.PARAMS_ERROR, "上传的图片文件不存在");
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();
    }

    @Override
    protected void processPictureFile(Object inputSource, File tempFile) {
        ThrowUtil.throwIf(inputSource == null, ResponseCode.PARAMS_ERROR, "上传的图片文件不存在");
        MultipartFile multipartFile = (MultipartFile) inputSource;
        try {
            multipartFile.transferTo(tempFile);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
