package com.leyou.common.exception;

import lombok.Getter;

/**
 * 自定义异常对象，来定义异常状态码
 */
@Getter
public class LyException extends RuntimeException{
    private Integer status;

    public LyException(Integer status, String message) {
        super(message);
        this.status = status;
    }

    public LyException(ExceptionEnum exceptionEnum) {
        super(exceptionEnum.getMessage());
        this.status = exceptionEnum.getStatus();
    }

}
