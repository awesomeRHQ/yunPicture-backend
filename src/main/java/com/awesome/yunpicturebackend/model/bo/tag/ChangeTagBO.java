package com.awesome.yunpicturebackend.model.bo.tag;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 标签改变结果实体
 */
@Data
public class ChangeTagBO implements Serializable {

    /**
     * 标签是否发生改变
     */
    private Boolean tagChangeStatus;

    /**
     * 新增的标签列表
     */
    private List<String> addTagList;

    /**
     * 删除的标签列表
     */
    private List<String> deleteTagList;

    private static final long serialVersionUID = 6806230041015425553L;

}
