package io.renren;

import io.jsonwebtoken.Claims;
import io.renren.modules.app.config.JwtUtil;
import io.renren.modules.app.utils.JwtUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;


@RunWith(SpringRunner.class)
@SpringBootTest
public class JwtTest {
    @Autowired
    private JwtUtils jwtUtils;
    @Test
    public void test() {
        String token = jwtUtils.generateToken(1);

        System.out.println(token);
    }

    @Resource
    private PasswordEncoder passwordEncoder;
    @Test
    public void TestBCryptPasswordEncoder(){
        String encode = passwordEncoder.encode("1234");
        String encode1 = passwordEncoder.encode("1234");
        System.out.println("加密后encode: "+ encode);
        System.out.println("加密后encode1: "+ encode1);
        boolean matches1 =
                passwordEncoder.matches("1234", "$2a$10$JDUXVpkPBx0wnA/xmULPUO.1Nba450KH9iWc3uQ4xFli4SCBl4QuG");
        boolean matches2 =
                passwordEncoder.matches("1234", "$2a$10$kuAmQpUE7RK2bRulrNINpejsiyRkM2VvjcApfbRt5zwmgz3ByTFDy");
        System.out.println("比对结果matches1 "+ matches1);
        System.out.println("比对结果matches2 "+ matches2);
    }

    
    @Test
    public void TestJwt() throws Exception {
        String jwt = JwtUtil.createJWT("123");
        System.out.println("加密后"+jwt);
        Claims claims = JwtUtil.parseJWT(jwt);
        String subject = claims.getSubject();
        System.out.println("解密后"+subject);
    }
}
