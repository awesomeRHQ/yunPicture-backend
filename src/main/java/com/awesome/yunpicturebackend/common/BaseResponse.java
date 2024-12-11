package com.awesome.yunpicturebackend.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class BaseResponse<T> implements Serializable {
    private static final long serialVersionUID = -4111478947822880259L;

    private Integer code;

    private T data;

    private String msg;

    public BaseResponse(Integer code, T data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }

    public BaseResponse(ResponseCode responseCode, T data) {
        this.code = responseCode.getCode();
        this.data = data;
        this.msg = responseCode.getMessage();
    }

    public BaseResponse(ResponseCode responseCode, T data, String message) {
        this.code = responseCode.getCode();
        this.data = data;
        this.msg = message;
    }

}
