package io.renren.modules.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.renren.modules.app.dao.MenuMapper;
import io.renren.modules.app.dao.UserDao;
import io.renren.modules.app.entity.UserEntity;
import io.renren.modules.app.entity.loginUserEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.List;
import java.util.Optional;

/**
 * @ClassName UserDetailServiceImpl
 * @Description TODO
 * @Author YuanJie
 * @Date 2022/8/25 12:45
 */
@Service
public class UserDetailServiceImpl implements UserDetailsService {
    @Resource
    UserDao userDao;
    @Resource
    private MenuMapper menuMapper;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        QueryWrapper<UserEntity> wrapper =
                new QueryWrapper<UserEntity>()
                        .eq("username", username);
        UserEntity user = userDao.selectOne(wrapper);
        if(!Optional.ofNullable(user).isPresent()){
            throw new UsernameNotFoundException("用户不存在!");
        }

        // 鉴权凭证
        List<String> list = menuMapper.selectPermsByUserId(user.getUserId());

        return new loginUserEntity(user,list);
    }
}
