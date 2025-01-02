package com.awesome.yunpicturebackend.model.dto.picture;

import com.awesome.yunpicturebackend.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PictureLoadMoreRequest extends PageRequest implements Serializable {

    /**
     * 搜索词（同时搜名称、简介等）
     */
    private String searchText;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;

    private static final long serialVersionUID = 1L;
}
