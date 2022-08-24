package io.renren.modules.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.modules.app.dao.UserSecurityTestDao;
import io.renren.modules.app.entity.UserSecurityTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * @ClassName UserSecurityTestImpl
 * @Description TODO
 * @Author YuanJie
 * @Date 2022/8/23 23:23
 */
@Service
public class UserSecurityTestImpl implements UserDetailsService {
    @Resource
    UserSecurityTestDao userSecurityTestDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        QueryWrapper<UserSecurityTest> wrapper =
                new QueryWrapper<UserSecurityTest>()
                        .eq("username", username);
        UserSecurityTest user = userSecurityTestDao.selectOne(wrapper);
        if(!Optional.ofNullable(user).isPresent()){
            throw new UsernameNotFoundException("用户不存在!");
        }
        // 鉴权凭证
        List<GrantedAuthority> role =
                // AuthorityUtils.commaSeparatedStringToAuthorityList("admins");  // 权限访问
                AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_sale,ROLE_admin"); // 基于角色访问需要前缀ROLE_
        return new User(user.getUsername(),
                new BCryptPasswordEncoder().encode(user.getPassword()),role);
    }
}
