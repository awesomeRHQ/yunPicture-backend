package com.awesome.yunpicturebackend.model.vo.picture;

import lombok.Data;

import java.util.List;

@Data
public class PictureTagCategory {

    /**
     * 分类列表
     */
    private List<String> categoryList;

    /**
     * 标签列表
     */
    private List<String> tagList;

}
