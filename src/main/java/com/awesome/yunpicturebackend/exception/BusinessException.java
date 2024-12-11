package com.awesome.yunpicturebackend.exception;

import com.awesome.yunpicturebackend.common.ResponseCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException{

    private int code;

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResponseCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ResponseCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

}
