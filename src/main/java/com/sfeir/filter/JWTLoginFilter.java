package com.sfeir.filter;

import com.sfeir.bean.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * @ClassName: JWTLoginFilter
 * @Description: 验证用户名密码正确后，生成一个token，并将token返回给客户端
 * * 该类继承自UsernamePasswordAuthenticationFilter，重写了其中的2个方法
 * * attemptAuthentication ：接收并解析用户凭证。
 * * successfulAuthentication ：用户成功登录后，这个方法会被调用，我们在这个方法里生成token。
 * @Author: Yang Naihua
 * @Create: 2019-01-20 13:55
 **/
public class JWTLoginFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;

    public JWTLoginFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    // 接收并解析用户凭证
    @Override
    public Authentication attemptAuthentication(HttpServletRequest req, HttpServletResponse res) throws AuthenticationException {
        try {
            User user = new ObjectMapper().readValue(req.getInputStream(), User.class);
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            user.getPassword(),
                            new ArrayList<>())
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 用户成功登录后，这个方法会被调用，我们在这个方法里生成token
    @Override
    protected void successfulAuthentication(HttpServletRequest req,
                                            HttpServletResponse res,
                                            FilterChain chain,
                                            Authentication auth) throws IOException, ServletException {
        // builder the token
        String token = null;
        try {
            token = Jwts.builder()
                    .setSubject(auth.getName())
//                    .setExpiration(new Date(System.currentTimeMillis() + 60 * 1000)) // 设置过期时间 60秒
                    .setExpiration(new Date(System.currentTimeMillis() + 60 * 60 * 24 * 365  * 1000)) // 设置过期时间
                    .signWith(SignatureAlgorithm.HS512, "spring-security-@Jwt!&Secret^#") //采用什么算法是可以自己选择的，不一定非要采用HS512
                    .compact();
            res.addHeader("Authorization", "Bearer " + token);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
