package com.awesome.yunpicturebackend.controller;

import com.awesome.yunpicturebackend.common.BaseResponse;
import com.awesome.yunpicturebackend.common.utils.ResultUtil;
import com.awesome.yunpicturebackend.model.bo.tag.ChangeTagBO;
import com.awesome.yunpicturebackend.model.entity.Picture;
import com.awesome.yunpicturebackend.service.PictureService;
import com.awesome.yunpicturebackend.service.TagService;
import com.awesome.yunpicturebackend.util.StringUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
public class MainController {

    @Resource
    private PictureService pictureService;

    /**
     * 后端调用健康检查接口
     * @return "ok"
     */
    @GetMapping("/health")
    public BaseResponse<String> health(){
        return ResultUtil.success("ok");
    }

    @GetMapping("/test")
    public BaseResponse<String> test(){
        String url = "https://awesomeyunpicture-1304989340.cos.ap-guangzhou.myqcloud.com/public/1867054181268410370/2024-12-24 18:52:04_IWFKcdBNTeuHNxE6.png";
        String prefix = "https://awesomeyunpicture-1304989340.cos.ap-guangzhou.myqcloud.com";
        String truncatedString = StringUtil.getTruncatedString(url, prefix);
        return ResultUtil.success(truncatedString);
    }

    @PostMapping("/test/delete/picture")
    public BaseResponse<Boolean> deletePictureTest(@RequestBody List<Long> pictureIds){
        List<Picture> pictureList = pictureService.listByIds(pictureIds);
        boolean b = pictureService.deletePictureByIds(pictureIds, true);
        return ResultUtil.success(b);
    }
}
