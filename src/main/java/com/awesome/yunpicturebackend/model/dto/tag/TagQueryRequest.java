package com.awesome.yunpicturebackend.model.dto.tag;

import com.awesome.yunpicturebackend.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

@Data
public class TagQueryRequest extends PageRequest implements Serializable {

    /**
     * 标签名称
     */
    private String name;

    /**
     * 使用次数
     */
    private Integer useCount;

    private static final long serialVersionUID = 2229525008294743087L;

}
