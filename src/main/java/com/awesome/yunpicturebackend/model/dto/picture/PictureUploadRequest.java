package com.awesome.yunpicturebackend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PictureUploadRequest implements Serializable {

    private String pictureName;

    private String category;

    private List<String> tagList;

    private Long spaceId;

    private static final long serialVersionUID = 1L;
}

