package com.awesome.yunpicturebackend.model.bo.picture;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class PictureUploadCustomInfo {

    private String pictureName;

    private String category;

    private List<String> tagList;

}
