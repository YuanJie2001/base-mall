package com.vector.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

/**
 * @ClassName ListValueConstrainValidator
 * 
 * @Author YuanJie
 * @Date 2022/7/3 19:19
 */
public class ListValueConstrainValidator implements ConstraintValidator<ListValue, Integer> {
    private Set<Integer> set = new HashSet<>();

    // 初始化方法
    @Override
    public void initialize(ListValue constraintAnnotation) {
        int[] values = constraintAnnotation.values();
        if (values != null && values.length != 0) {
            for (int value : values) {
                set.add(value);
            }
        }

    }

    // 判断是否校验成功

    /**
     * @param value                      需要校验的值
     * @param constraintValidatorContext 需要校验的上下文环境信息
     * @return
     */
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        return set.contains(value);
    }
}
