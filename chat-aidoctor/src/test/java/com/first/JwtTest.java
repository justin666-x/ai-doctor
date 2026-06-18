package com.first;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {

    @Test
    public void testGen() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", "1");
        claims.put("username", "zhangsan");
        //生成jwt代码
        String token = JWT.create().withClaim("user", claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + 60 * 1000* 60))//添加过期时间
                .sign(Algorithm.HMAC256("cjx"));//指定算法,配置秘钥

        System.out.println(token);
    }
    @Test
    public void testParse() {
        //定义字符串,模拟用户传来的token
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9." +
                "eyJjbGFpbXMiOnsiaWQiOjcsInVzZXJuYW1lIjoid3d3d3cifSwiZXhwIjoxNzUwNzg4Njg0fQ." +
                "0ek6nxi9li09XLCg7CarvCslBi3H4UNjpUmNyGCgYno";
        //验证token
       JWTVerifier jwtVerifier =  JWT.require(Algorithm.HMAC256("cjx")).build();

      DecodedJWT decodedJWT = jwtVerifier.verify(token);//验证token是否有效,生成一个解析后的jwt对象
      Map<String, Claim> claims = decodedJWT.getClaims();
      System.out.println(claims.get("user"));//获取用户信息
    }
}