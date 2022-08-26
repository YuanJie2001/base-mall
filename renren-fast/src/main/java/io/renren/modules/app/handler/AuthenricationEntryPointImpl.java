package io.renren.modules.app.handler;

import com.alibaba.fastjson.JSON;
import io.renren.common.utils.R;
import io.renren.modules.app.config.WebUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @ClassName AuthenricationEntryPointImpl
 * @Description 认证失败异常处理器
 * @Author YuanJie
 * @Date 2022/8/26 7:54
 */
@Component
public class AuthenricationEntryPointImpl implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        R r = R.error(HttpStatus.FORBIDDEN.value(), "用户认证失败请查询登录");
        String s = JSON.toJSONString(r);
        // 处理异常
        WebUtils.renderString(response,s);
    }
}
