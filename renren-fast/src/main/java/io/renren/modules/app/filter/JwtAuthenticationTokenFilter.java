package io.renren.modules.app.filter;

import io.jsonwebtoken.Claims;
import io.renren.modules.app.config.JwtUtil;
import io.renren.modules.app.entity.loginUserEntity;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.web.servlet.OncePerRequestFilter;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * @ClassName JwtAuthenticationTokenFilter
 * @Description jwt token回传解密验证数据库信息.
 * @Author YuanJie
 * @Date 2022/8/25 20:43
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    @Resource
    private RedissonClient redissonClient;
    @Override
    protected void doFilterInternal(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        // 获取token
        String token = request.getHeader("token");
        if (StringUtils.isEmpty(token)) {
            filterChain.doFilter(request, response);
            return;
        }
        // 解析token
        String subject;
        try {
            Claims claims = JwtUtil.parseJWT(token);
            subject = claims.getSubject();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("token非法");
        }
        // 从redis中获取用户信息 对应登录加密存储redis的键
        String redisKey = "token:" + subject;
        // redis的redisson整合框架
        RBucket<Object> bucket = redissonClient.getBucket(redisKey);
        loginUserEntity loginUserEntity = (loginUserEntity) bucket.get();
        if(!Optional.ofNullable(loginUserEntity).isPresent()){
            throw new RuntimeException("token非法");
        }
        // 存入SecurityContextHolder,让后续的过滤器链获取信息
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUserEntity, null, loginUserEntity.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        filterChain.doFilter(request, response);
    }
}
