package com.awesome.yunpicturebackend.model.dto.picture;

import com.awesome.yunpicturebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class PictureQueryRequest extends PageRequest implements Serializable {

    /**
     * 搜索词（同时搜名称、简介等）
     */
    private String searchText;

    private List<String> tags;

    private String category;

    private static final long serialVersionUID = 1L;

}