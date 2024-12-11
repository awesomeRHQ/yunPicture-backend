package com.awesome.yunpicturebackend.controller;

import com.awesome.yunpicturebackend.common.BaseResponse;
import com.awesome.yunpicturebackend.common.utils.ResultUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MainController {

    /**
     * 后端调用健康检查接口
     * @return "ok"
     */
    @GetMapping("/health")
    public BaseResponse<String> health(){
        return ResultUtil.success("ok");
    }
}
