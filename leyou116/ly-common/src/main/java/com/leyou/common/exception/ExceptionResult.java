package com.leyou.common.exception;

import lombok.Getter;
import org.joda.time.DateTime;

/**
 * @author 黑马程序员
 * 异常时返回一个对象
 */
@Getter
public class ExceptionResult {
    private int status;
    private String message;
    private String timestamp;

    public ExceptionResult(LyException e) {
        this.status = e.getStatus();
        this.message = e.getMessage();
        this.timestamp = DateTime.now().toString("yyyy-MM-dd HH:mm:ss");
    }
}