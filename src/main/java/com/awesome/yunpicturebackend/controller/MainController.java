package com.awesome.yunpicturebackend.controller;

import com.awesome.yunpicturebackend.common.BaseResponse;
import com.awesome.yunpicturebackend.common.utils.ResultUtil;
import com.awesome.yunpicturebackend.model.bo.tag.ChangeTagBO;
import com.awesome.yunpicturebackend.service.TagService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
public class MainController {

    @Resource
    private TagService tagService;

    /**
     * 后端调用健康检查接口
     * @return "ok"
     */
    @GetMapping("/health")
    public BaseResponse<String> health(){
        return ResultUtil.success("ok");
    }

    @GetMapping("/test")
    public BaseResponse<ChangeTagBO> test(){
        Long pictureId = 1871509149044039681L;
        List<String> newTagList = new ArrayList<>();
        newTagList.add("建模");
        newTagList.add("入门");
        newTagList.add("新增");
        ChangeTagBO pictureTagsChange = tagService.isPictureTagsChange(pictureId, newTagList);
        return ResultUtil.success(pictureTagsChange);
    }
}
