package com.awesome.yunpicturebackend.model.dto.picture;

import com.awesome.yunpicturebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 管理员图片查询请求类
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PictureAdminQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;

    /**
     * 文件体积
     */
    private Long picSize;


    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 搜索词（同时搜名称、简介等）
     */
    private String searchText;

    /**
     * 审核状态
     */
    private List<Integer> reviewStatus;

    /**
     * 用户 id
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