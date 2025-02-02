package com.awesome.yunpicturebackend.model.dto.file;

import lombok.Data;

import java.io.Serializable;

@Data
public class UploadPictureResult implements Serializable {

    /**
     * 图片url
     */
    private String url;

    /**
     * 压缩图片url
     */
    private String compressUrl;

    /**
     * 图片名称
     */
    private String name;

    /**
     * 图片大小
     */
    private Long picSize;

    /**
     * 图片宽度
     */
    private Integer picWidth;

    /**
     * 图片高度
     */
    private Integer picHeight;

    /**
     * 图片宽高比
     */
    private Double picScale;

    /**
     * 图片格式
     */
    private String picFormat;

    /**
     * 图片主色调
     */
    private String picColor;

    private static final long serialVersionUID = 4125079506489694359L;

}
