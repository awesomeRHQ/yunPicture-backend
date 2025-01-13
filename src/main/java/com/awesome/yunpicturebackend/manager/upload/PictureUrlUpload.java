package com.awesome.yunpicturebackend.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.awesome.yunpicturebackend.common.ResponseCode;
import com.awesome.yunpicturebackend.exception.ThrowUtil;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * 图片地址上传
 */
@Service
public class PictureUrlUpload extends PictureUploadTemplate{

    @Override
    protected void validPicture(Object inputSource) {
        // 校验参数
        ThrowUtil.throwIf(inputSource == null, ResponseCode.PARAMS_ERROR, "图片地址不存在");
        String fileUrl = (String) inputSource;
        // 校验URL格式
        try {
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        // 校验URL协议
        ThrowUtil.throwIf(!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://"),
                ResponseCode.PARAMS_ERROR,
                "仅支持 HTTP 或 HTTPS 协议的文件地址"
        );
        // 发送HEAD请求以验证文件是否存在
        HttpResponse response = null;
        try {
            response = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            ThrowUtil.throwIf(response.getStatus() != HttpStatus.HTTP_OK,
                    ResponseCode.NOT_FOUND_ERROR,
                    "文件获取失败");
            // 校验图片类型
            String contentType = response.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)) {
                // 允许的文件类型
                List<String> allowTypeList = Arrays.asList("image/jpg", "image/jpeg", "image/png", "image/webp");
                ThrowUtil.throwIf(!allowTypeList.contains(contentType), ResponseCode.PARAMS_ERROR, "文件类型错误");
            }
            // 校验图片大小
            long pictureSize = response.contentLength();
            final long SIX_MB = 6 * 1024 * 1024L;
            ThrowUtil.throwIf(pictureSize > SIX_MB, ResponseCode.PARAMS_ERROR, "图片大小不能超过6MB");
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    @Override
    protected String getOriginalFilename(Object inputSource) {
        ThrowUtil.throwIf(inputSource == null, ResponseCode.PARAMS_ERROR, "图片地址不存在");
        String fileUrl = (String) inputSource;
        return FileUtil.mainName(fileUrl);
    }

    @Override
    protected void processPictureFile(Object inputSource, File tempFile) {
        ThrowUtil.throwIf(inputSource == null, ResponseCode.PARAMS_ERROR, "图片地址不存在");
        String fileUrl = (String) inputSource;
        HttpUtil.downloadFile(fileUrl, tempFile);
    }

}
