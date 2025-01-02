package com.awesome.yunpicturebackend.controller;

import com.awesome.yunpicturebackend.common.BaseResponse;
import com.awesome.yunpicturebackend.common.ResponseCode;
import com.awesome.yunpicturebackend.common.utils.ResultUtil;
import com.awesome.yunpicturebackend.exception.ThrowUtil;
import com.awesome.yunpicturebackend.model.dto.tag.TagQueryRequest;
import com.awesome.yunpicturebackend.model.entity.Tag;
import com.awesome.yunpicturebackend.service.TagService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/tag")
public class TagController {

    @Resource
    private TagService tagService;

    @PostMapping("/hot_tags")
    public BaseResponse<List<String>> listHotTagName(@RequestBody TagQueryRequest tagQueryRequest) {
        ThrowUtil.throwIf(tagQueryRequest == null, ResponseCode.PARAMS_ERROR);
        int current = tagQueryRequest.getCurrent();
        int pageSize = tagQueryRequest.getPageSize();
        Page<Tag> page = tagService.page(new Page<>(current, pageSize), tagService.getQueryWrapper(tagQueryRequest));
        List<Tag> tagList = page.getRecords();
        ArrayList<String> hotTagNameList = new ArrayList<>();
        tagList.forEach(tag -> hotTagNameList.add(tag.getName()));
        return ResultUtil.success(hotTagNameList);
    }

}
