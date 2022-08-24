package io.renren.modules.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * @ClassName SecurityConfigTest
 * @Description TODO
 * @Author YuanJie
 * @Date 2022/8/24 10:34
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Resource
    private UserDetailsService userDetailsService;
    @Resource
    private DataSource dataSource;
    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
//        jdbcTokenRepository.setCreateTableOnStartup(true);
        return jdbcTokenRepository;
    }
    @Bean
    // 注入BCryptPasswordEncoder编码器
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
    @Override
    // 认证管理器
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    // 重写configure(HttpSecurity http) 自定义登录页面
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 退出 logoutUrl()发送数据的接口地址  logoutSuccessUrl()页面跳转的地址
        http.logout().logoutUrl("/logout").logoutSuccessUrl("/index").permitAll();
        // 配置无权限访问自定义403界面
        http.exceptionHandling().accessDeniedPage("/unauth.html");
        http.formLogin() // 自定义自己编写的登录页面
                .loginPage("/login.html") // 登录页面设置
                .loginProcessingUrl("/user/login") // 登录数据跳到接口地址
                .defaultSuccessUrl("/test/index").permitAll()// 成功校验后页面跳转的地址
                .and()
                .authorizeRequests()
                .antMatchers("/","/test/hello","/user/login").permitAll() // 放行的url
                // .antMatchers("/test/index").hasAnyAuthority("manager,admins") // 只有admins权限才能访问该资源
                .antMatchers("/test/index").hasAnyRole("sale,admin") // 根据角色访问
                .anyRequest().authenticated() // 除上述放行的url,其余全部鉴权认证
                .and()
                .rememberMe().tokenRepository(persistentTokenRepository())
                .tokenValiditySeconds(60) // 设置token有效期60s
                .userDetailsService(userDetailsService)
                .and()
                .csrf().disable();

    }
}
