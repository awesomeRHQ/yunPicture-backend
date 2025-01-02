package com.awesome.yunpicturebackend.model.bo.collection;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
public class AddAndDeleteResult implements Serializable {

    List<String> addList;

    List<String> deleteList;

}
