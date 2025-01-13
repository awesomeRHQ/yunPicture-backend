package com.awesome.yunpicturebackend.model.dto.picture;

import lombok.Data;

import java.util.List;

@Data
public class PictureUploadByBatchRequest {

    /**
     * 爬取关键词
     */
    private String searchText;

    /**
     * 爬取数量
     */
    private Integer searchCount;

    /**
     * 图片名称（默认值为关键词）
     */
    private String pictureName;

    /**
     * 图片分类
     */
    private String category;

    /**
     * 图片标签
     */
    private List<String> tagList;

}
