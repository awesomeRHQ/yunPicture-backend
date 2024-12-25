package com.awesome.yunpicturebackend.controller;

import com.awesome.yunpicturebackend.common.BaseResponse;
import com.awesome.yunpicturebackend.common.ResponseCode;
import com.awesome.yunpicturebackend.common.utils.ResultUtil;
import com.awesome.yunpicturebackend.exception.BusinessException;
import com.awesome.yunpicturebackend.manager.CosManager;
import com.qcloud.cos.model.PutObjectResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private CosManager cosManager;

    @PostMapping("/test/upload")
    public BaseResponse<String> testUploadFileToCos(@RequestPart MultipartFile multipartFile) {
        // 从前端上传文件的流中获取到文件名
        String fileName = multipartFile.getOriginalFilename();
        String filePath = String.format("/test/%s",fileName);
        // 1.创建一个临时文件
        File tempFile = null;
        try {
            // 2.将上传的文件保存到服务器的临时文件中
            tempFile = File.createTempFile(filePath, null);
            multipartFile.transferTo(tempFile);
            // 3.将临时文件中的内容保存到cos
            cosManager.putObject(filePath, tempFile);
            return ResultUtil.success(filePath);
        } catch (IOException e) {
            log.error("file upload error; filePath = " + filePath);
            throw new BusinessException(ResponseCode.OPERATION_ERROR,"文件上传失败");
        } finally {
            // 4.删除临时文件
            if (tempFile != null) {
                boolean deleteResult = tempFile.delete();
                if (!deleteResult) {
                    log.error("tempFile delete error; filePath = " + filePath);
                }
            }
        }
    }
}
