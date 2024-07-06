package com.atguigu.lease.common.utlis;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JWTUtil {

    private static SecretKey key = Keys.hmacShaKeyFor("VpVXAvR9pbtzpdqKLxtWsdUsOSYdNjjf".getBytes());

    public static String createJwtToken(Long id,String name){
        String token = Jwts.builder()
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 单位毫秒
                .setSubject("LOGIN_USER")
                .claim("id", id) //  自定义字段
                .claim("username", name)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        return token;
    }



    public static void main(String[] args) {
        System.out.println(createJwtToken(1L, "admin"));
    }
}
