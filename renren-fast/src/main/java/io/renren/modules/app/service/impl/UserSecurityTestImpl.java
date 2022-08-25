package io.renren.modules.app.service.impl;


import io.renren.common.utils.R;
import io.renren.modules.app.config.JwtUtil;
import io.renren.modules.app.entity.loginUserEntity;
import io.renren.modules.app.service.UserSecurityService;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName UserSecurityTestImpl
 * @Description TODO
 * @Author YuanJie
 * @Date 2022/8/23 23:23
 */
@Service
public class UserSecurityTestImpl implements UserSecurityService {


    @Resource
    private AuthenticationManager authenticationManager;
    @Resource
    private RedissonClient redissonClient;
    @Override
    public R login(loginUserEntity loginUserEntity) {
        // Authentication.Manager authenticate进行用户认证
        /**
         * UsernamePasswordAuthenticationToken继承AbstractAuthenticationToken实现Authentication
         * 所以当在页面中输入用户名和密码之后首先会进入到UsernamePasswordAuthenticationToken验证(Authentication)，
         * 然后生成的Authentication会被交由AuthenticationManager来进行管理
         * 而AuthenticationManager管理一系列的AuthenticationProvider，
         * 而每一个Provider都会通UserDetailsService和UserDetail来返回一个
         * 以UsernamePasswordAuthenticationToken实现的带用户名和密码以及权限的Authentication
         */
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUserEntity.getUsername(), loginUserEntity.getPassword());
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);
        // 如果认证没通过，给出对应的提示
        if(!Optional.ofNullable(authenticate).isPresent()){
            throw new UsernameNotFoundException(("登录失败"));
        }
        // 如果认证通过了，使用userid生成一个jwt jwt存入R返回
        loginUserEntity loginUser = (loginUserEntity) authenticate.getPrincipal();
        String userId = loginUser.getId().toString();
        String jwt = JwtUtil.createJWT(userId);
        Map<String, Object> map = new HashMap<>();
        map.put("token",jwt);

        // 把完整的用户信息存入redis userid作为key
        RBucket<Object> token = redissonClient.getBucket("token:"+userId);
        token.set(loginUser,30, TimeUnit.MINUTES);
        return R.ok(map);
    }

    @Override
    public R logout() {
        // 获取SecurityContextHolder中的用户id
        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        loginUserEntity loginUser = (loginUserEntity) authentication.getPrincipal();
        String userId = loginUser.getId().toString();
        // 删除redis中的值
        RBucket<Object> bucket = redissonClient.getBucket("token:" + userId);
        bucket.delete();
        return R.ok("退出成功!");
    }
}
