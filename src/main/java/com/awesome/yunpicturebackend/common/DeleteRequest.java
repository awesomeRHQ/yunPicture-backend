package com.awesome.yunpicturebackend.common;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DeleteRequest implements Serializable {

    private static final long serialVersionUID = 5766113511041907565L;

    private Long id;

    private List<Long> ids;

}
