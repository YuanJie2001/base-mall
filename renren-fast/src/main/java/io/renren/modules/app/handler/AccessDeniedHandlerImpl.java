package io.renren.modules.app.handler;

import com.alibaba.fastjson.JSON;
import io.renren.common.utils.R;
import io.renren.modules.app.config.WebUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @ClassName AccessDeniedHandlerImpl
 * @Description 授权失败异常处理器
 * @Author YuanJie
 * @Date 2022/8/26 8:09
 */
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        R r = R.error(HttpStatus.UNAUTHORIZED.value(), "您的权限不足");
        String s = JSON.toJSONString(r);
        // 处理异常
        WebUtils.renderString(response,s);
    }
}
