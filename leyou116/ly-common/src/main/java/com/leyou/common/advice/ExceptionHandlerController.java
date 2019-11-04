package com.leyou.common.advice;

import com.leyou.common.exception.ExceptionResult;
import com.leyou.common.exception.LyException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionHandlerController {

    /**
     * ExceptionHandler(LyException.class)
     * 表示当前处理器只处理LyException异常
     * @return
     */
    @ExceptionHandler(LyException.class)
    public ResponseEntity<ExceptionResult> handlerException(LyException e){
        return ResponseEntity.status(e.getStatus()).body(new ExceptionResult(e));
    }

}
