package io.renren.modules.app.controller;

import io.renren.common.utils.R;
import io.renren.modules.app.entity.loginUserEntity;
import io.renren.modules.app.service.UserSecurityService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/test")
public class SecurityTest {
    @Resource
    private UserSecurityService userSecurityService;

    @PostMapping("/user/login")
    public R login(@RequestBody loginUserEntity loginUserEntity){
        R r = userSecurityService.login(loginUserEntity);
        return R.ok();
    }

    @GetMapping("/user/logout")
    public  R logout(){
        return userSecurityService.logout();
    }

    @GetMapping("/hello")
    @PreAuthorize("hasAuthority('system:dept:list')")
    public String hello(){
        return "hello";
    }
}