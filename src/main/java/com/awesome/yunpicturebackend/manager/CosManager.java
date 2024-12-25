package com.awesome.yunpicturebackend.manager;

import com.awesome.yunpicturebackend.common.ResponseCode;
import com.awesome.yunpicturebackend.config.CosClientConfig;
import com.awesome.yunpicturebackend.exception.BusinessException;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;

/**
 * 可复用的cos操作类
 */
@Component
@Slf4j
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 将本地文件上传到 COS
     * @param key 对象在存储桶中的唯一标识
     * @param file 本地文件
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        PutObjectResult putObjectResult = null;
        try {
            putObjectResult = cosClient.putObject(putObjectRequest);
        }catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(ResponseCode.OPERATION_ERROR,"文件上传Cos服务器失败");
        }finally {
            cosClient.shutdown();
        }
        return putObjectResult;
    }
    // todo 直接将文件以流到方式上传到cos

    /**
     * 将本地文件上传到 COS
     * @param key 对象在存储桶中的唯一标识
     * @param file 本地文件
     */
    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        try {
            // 对图片进行处理（获取基本信息也视为一种图片处理）
            PicOperations picOperations = new PicOperations();
            // 1表示返回图片信息
            picOperations.setIsPicInfo(1);
            // 构造处理参数
            putObjectRequest.setPicOperations(picOperations);
            return cosClient.putObject(putObjectRequest);
        }catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException(ResponseCode.OPERATION_ERROR,"图片上传Cos服务器失败");
        }finally {
            cosClient.shutdown();
        }

    }
}
