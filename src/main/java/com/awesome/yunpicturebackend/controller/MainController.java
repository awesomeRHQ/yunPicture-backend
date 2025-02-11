package com.awesome.yunpicturebackend.controller;

import com.awesome.yunpicturebackend.annotation.ModulePermissionCheck;
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
@RequestMapping("/main")
public class MainController {

    /**
     * 后端调用健康检查接口
     * @return "ok"
     */
    @GetMapping("/add/health")
    @ModulePermissionCheck()
    public BaseResponse<String> health(){
        return ResultUtil.success("ok");
    }
}
