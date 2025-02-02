package com.awesome.yunpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PictureEditRequest implements Serializable {

    // todo 用户可以选择是否公开图库图片


    /**
     * id
     */
    private Long id;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 图片上传用户
     */
    private Long userId;

    /**
     * 所属空间Id
     */
    private Long spaceId;

    /**
     * 是否公开（用于用户图库）：0-不公开；1-公开
     */
    private Integer doPub;

    private static final long serialVersionUID = 1L;

}