package com.vector.mallproduct.exception;

import com.vector.common.exception.BizCodeEnume;
import com.vector.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName mallExceptionControllerAdvice
 * @Description 集中处理抛出的所有异常
 * @Author YuanJie
 * @Date 2022/7/3 17:26
 */

/**
 * 扫描异常处理的范围
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.vector.mallproduct.controller")
public class mallExceptionControllerAdvice {
    /**
     * 指定可以处理的异常类型
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleVaildException(MethodArgumentNotValidException e) {
        log.error("数据校验出现的问题{},异常类型:{}", e.getMessage(), e.getClass());
        BindingResult result = e.getBindingResult();
        Map<String, String> errorMap = new HashMap<>();
        // 1. 获取校验结果
        result.getFieldErrors().forEach((item) -> {
            // 2. 获取错误提示
            String message = item.getDefaultMessage();
            // 3. 获取错误的属性名
            String field = item.getField();
            errorMap.put(field, message);
        });
        return R.error(BizCodeEnume.VALID_EXCEPTION.getCode(), BizCodeEnume.VALID_EXCEPTION.getMsg()).put("data", errorMap);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable throwable) {
        log.error("错误: ", throwable);
        return R.error(BizCodeEnume.UNKNOW_EXCEPTION.getCode(), BizCodeEnume.UNKNOW_EXCEPTION.getMsg());
    }


}
